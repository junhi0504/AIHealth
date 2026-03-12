document.addEventListener("DOMContentLoaded", () => {
    const dateInput = document.getElementById("measurementDate");
    const form = document.getElementById("inbodyForm");

    if (dateInput) {
        dateInput.addEventListener("change", () => {
            const date = dateInput.value;
            if (!date) return;

            fetch(`/inbody/api/data?date=${date}`)
                .then(res => res.ok ? res.json() : null)
                .then(data => {
                    const fields = ["height", "weight", "bodyFatPercentage", "muscleMass", "visceralFatLevel", "goal"];
                    fields.forEach(id => {
                        document.getElementById(id).value = data ? data[id] ?? "" : "";
                        if (!data) document.getElementById(id).placeholder = "";
                    });
                })
                .catch(err => console.error("데이터 로드 오류:", err));
        });
    }
});
