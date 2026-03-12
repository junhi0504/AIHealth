document.addEventListener('DOMContentLoaded', () => {
    // Chart.js 인스턴스를 저장할 변수들
    let weightChart, muscleChart, fatChart;
    // 서버에서 받은 전체 인바디 데이터를 저장할 배열
    let fullInbodyData = [];

    /**
     * 날짜 객체를 'YYYY-MM-DD' 형식의 문자열로 변환하는 함수
     * @param {Date | string} date - 변환할 날짜
     * @returns {string} 포맷팅된 날짜 문자열
     */
    function formatDate(date) {
        if (!date) return '';
        const d = new Date(date);
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    }

    /**
     * 차트를 생성하고 업데이트하는 함수
     * @param {string[]} dates - 차트의 x축 레이블 (날짜)
     * @param {number[]} weights - 체중 데이터
     * @param {number[]} muscleMass - 골격근량 데이터
     * @param {number[]} bodyFat - 체지방률 데이터
     */
    function renderCharts(dates, weights, muscleMass, bodyFat) {
        const options = {
            responsive: true,
            devicePixelRatio: 3,
            plugins: {
                legend: {
                    display: true,
                    position: 'top',
                    labels: {
                        font: {
                            size: 12, // 범례 폰트 크기 줄임 (14 -> 12)
                            weight: 'bold'
                        }
                    }
                }
            },
            scales: {
                x: {
                    ticks: {
                        autoSkip: true,
                        maxTicksLimit: 7,
                        font: {
                            size: 10, // X축 폰트 크기 줄임 (12 -> 10)
                            weight: 'bold'
                        }
                    },
                    title: {
                        display: false
                    }
                },
                y: {
                    ticks: {
                        font: {
                            size: 10, // Y축 폰트 크기 줄임 (12 -> 10)
                            weight: 'bold'
                        }
                    },
                    title: {
                        display: false
                    }
                }
            }
        };

        // ID를 기반으로 기존 차트가 있는지 확인하고 파괴합니다.
        ['weightChart', 'muscleChart', 'fatChart'].forEach(id => {
            const chart = Chart.getChart(id);
            if (chart) {
                chart.destroy();
            }
        });

        weightChart = new Chart(document.getElementById('weightChart'), {
            type: 'line',
            data: {
                labels: dates,
                datasets: [{
                    label: '체중 (kg)',
                    data: weights,
                    borderColor: '#007bff',
                    backgroundColor: 'rgba(0,123,255,0.1)',
                    fill: true,
                    tension: 0.3
                }]
            },
            options
        });

        muscleChart = new Chart(document.getElementById('muscleChart'), {
            type: 'line',
            data: {
                labels: dates,
                datasets: [{
                    label: '골격근량 (kg)',
                    data: muscleMass,
                    borderColor: '#28a745',
                    backgroundColor: 'rgba(40,167,69,0.1)',
                    fill: true,
                    tension: 0.3
                }]
            },
            options
        });

        fatChart = new Chart(document.getElementById('fatChart'), {
            type: 'line',
            data: {
                labels: dates,
                datasets: [{
                    label: '체지방률 (%)',
                    data: bodyFat,
                    borderColor: '#dc3545',
                    backgroundColor: 'rgba(220,53,69,0.1)',
                    fill: true,
                    tension: 0.3
                }]
            },
            options
        });
    }

    /**
     * 인바디 데이터 테이블을 렌더링하는 함수
     * @param {object[]} dataToRender - 테이블에 표시할 데이터 배열
     */
    function renderTable(dataToRender) {
        const tableBody = document.getElementById('inbodyTableBody');
        tableBody.innerHTML = ''; // 기존 내용을 초기화

        if (dataToRender.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="6">해당 날짜에 측정한 인바디 정보가 없습니다.</td></tr>`;
            return;
        }

        dataToRender.forEach(r => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${formatDate(r.measurementDate)}</td>
                <td>${(r.height ?? 0).toFixed(1)}</td>           <!-- 키 추가 -->
                <td>${(r.weight ?? 0).toFixed(1)}</td>
                <td>${(r.muscleMass ?? 0).toFixed(1)}</td>
                <td>${(r.bodyFatPercentage ?? 0).toFixed(1)}</td>
                <td>${r.visceralFatLevel ?? 0}</td>               <!-- 내장지방 추가 -->
            `;
            tableBody.appendChild(tr);
        });
    }


    /**
     * 서버에서 데이터를 가져와 차트와 테이블을 표시하는 메인 함수
     */
    async function fetchAndDisplayData() {
        const memberIdEl = document.getElementById('memberId');
        if (!memberIdEl) return;
        const memberId = memberIdEl.value;

        const graphSection = document.getElementById('graphSection');
        const tableSection = document.getElementById('tableSection');
        const noDataMessage = document.getElementById('noDataMessage');

        try {
            const response = await fetch(`${window.location.origin}/main/profile/inbodyRecords/${memberId}`);
            if (!response.ok) throw new Error('데이터 요청 실패');
            fullInbodyData = await response.json();

            if (!fullInbodyData || fullInbodyData.length === 0) {
                graphSection.style.display = 'none';
                tableSection.style.display = 'none';
                noDataMessage.style.display = 'block';
                return;
            }

            // 날짜순으로 정렬 (오래된 데이터 -> 최신 데이터)
            fullInbodyData.sort((a, b) => new Date(a.measurementDate) - new Date(b.measurementDate));

            // 차트에는 최근 7개의 데이터를 사용
            const recentDataForChart = fullInbodyData.slice(-7);
            const dates = recentDataForChart.map(r => formatDate(r.measurementDate));
            const weights = recentDataForChart.map(r => r.weight ?? 0);
            const muscleMass = recentDataForChart.map(r => r.muscleMass ?? 0);
            const bodyFat = recentDataForChart.map(r => r.bodyFatPercentage ?? 0);

            // 테이블에는 최근 5개의 데이터를 최신순으로 표시
            const recentDataForTable = fullInbodyData.slice(-5).reverse();
            renderTable(recentDataForTable);

            // 차트와 테이블 렌더링
            renderCharts(dates, weights, muscleMass, bodyFat);

            graphSection.style.display = 'flex';
            tableSection.style.display = 'block';
            noDataMessage.style.display = 'none';

        } catch (err) {
            console.error(err);
        }
    }

    // 날짜 선택기 이벤트 리스너
    const datePicker = document.getElementById('datePicker');
    if (datePicker) {
        datePicker.addEventListener('change', (e) => {
            const selectedDate = e.target.value;
            if (!selectedDate) {
                // 날짜 선택이 해제되면 최근 5개 데이터를 다시 표시
                const recentData = fullInbodyData.slice(-5).reverse();
                renderTable(recentData);
                return;
            }
            // 선택된 날짜에 해당하는 데이터만 필터링하여 테이블에 표시
            const filteredData = fullInbodyData.filter(r => formatDate(r.measurementDate) === selectedDate);
            renderTable(filteredData);
        });
    }

    // '전체보기' 버튼 이벤트 리스너
    const viewAllBtn = document.getElementById('viewAllBtn');
    if (viewAllBtn) {
        viewAllBtn.addEventListener('click', () => {
            datePicker.value = ''; // 날짜 선택 초기화
            // 전체 데이터를 최신순으로 정렬하여 테이블에 표시
            renderTable(fullInbodyData.slice().reverse());
        });
    }

    // 페이지 로드 시 데이터 가져오기 실행
    fetchAndDisplayData();
});