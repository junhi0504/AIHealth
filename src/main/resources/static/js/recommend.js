document.addEventListener("DOMContentLoaded", function() {
    const btn = document.getElementById('recommendBtn');
    if (!btn) return; // 로그인 안 됐거나 버튼이 없으면 종료

    btn.addEventListener('click', async function() {
        const boardId = btn.dataset.boardId;
        const csrfHeader = btn.dataset.csrfHeader; // ex: "X-CSRF-TOKEN"
        const csrfToken = btn.dataset.csrfToken;

        if (!boardId) {
            console.error('recommend: boardId가 없습니다.');
            return;
        }

        const headers = {'Content-Type': 'application/json'};
        if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;

        try {
            const res = await fetch(`/board/${boardId}/recommend`, {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({})
            });

            if (!res.ok) {
                const text = await res.text();
                console.error('추천 요청 실패:', res.status, text);
                if (res.status === 403) {
                    alert('추천 실패: 권한/CSRF 문제(403). 개발자 콘솔의 네트워크/응답 확인하세요.');
                } else if (res.status === 404) {
                    alert('추천 API를 찾을 수 없습니다(404). URL/컨트롤러 경로 확인하세요.');
                }
                return;
            }

            const data = await res.json();
            const countEl = document.getElementById('recommendCount');
            if (countEl && data.recommendCount != null) countEl.innerText = data.recommendCount;

        } catch (err) {
            console.error('추천 요청 중 네트워크 오류:', err);
            alert('네트워크 오류가 발생했습니다. 개발자 콘솔 확인하세요.');
        }
    });
});
