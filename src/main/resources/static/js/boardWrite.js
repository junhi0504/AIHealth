document.addEventListener("DOMContentLoaded", function () {
    const addImageBtn = document.getElementById('addImageBtn');
    const imageInput = document.getElementById('imageInput');
    const imageDisplay = document.getElementById('imageDisplay');
    const imageDataInput = document.getElementById('imageData');

    if (!addImageBtn || !imageInput || !imageDisplay || !imageDataInput) return;

    const newImages = [];
    const deleteExistingImageIds = [];

    // ------------------ 이미지 추가 ------------------
    addImageBtn.addEventListener('click', () => imageInput.click());

    imageInput.addEventListener('change', function () {
        Array.from(this.files).forEach(file => {
            const reader = new FileReader();
            reader.onload = function (e) {
                const base64 = e.target.result;
                newImages.push(base64);

                const div = document.createElement('div');
                div.classList.add('preview-image-container');

                const img = document.createElement('img');
                img.src = base64;
                img.style.width = '150px';
                img.style.height = '150px';
                img.style.objectFit = 'cover';
                img.style.margin = '5px';

                const removeBtn = document.createElement('button');
                removeBtn.type = 'button';
                removeBtn.textContent = '삭제';
                removeBtn.classList.add('remove-new-image');

                div.appendChild(img);
                div.appendChild(removeBtn);
                imageDisplay.appendChild(div);

                imageDataInput.value = JSON.stringify(newImages);
            };
            reader.readAsDataURL(file);
        });
        this.value = '';
    });

    // ------------------ 이미지 삭제 ------------------
    imageDisplay.addEventListener('click', function (e) {
        // 기존 이미지 삭제
        const existingRemoveBtn = e.target.closest('.remove-existing-image');
        if (existingRemoveBtn) {
            const imgId = existingRemoveBtn.getAttribute('data-id');
            if (imgId && !deleteExistingImageIds.includes(imgId)) {
                deleteExistingImageIds.push(imgId);
            }
            existingRemoveBtn.closest('.existing-image').remove();
            return;
        }

        // 새로 추가한 이미지 삭제
        const newRemoveBtn = e.target.closest('.remove-new-image');
        if (newRemoveBtn) {
            const parentDiv = newRemoveBtn.closest('.preview-image-container');
            const base64 = parentDiv.querySelector('img').src;
            const index = newImages.indexOf(base64);
            if (index > -1) newImages.splice(index, 1);
            parentDiv.remove();
            imageDataInput.value = JSON.stringify(newImages);
        }
    });

    // ------------------ 폼 제출 시 처리 ------------------
    const form = imageDisplay.closest('form');
    if (form) {
        form.addEventListener('submit', () => {
            // 새로 업로드된 이미지
            imageDataInput.value = JSON.stringify(newImages);

            // 기존 이미지 삭제용 hidden input
            let deleteInput = form.querySelector('input[name="deleteImageIdsString"]');
            if (!deleteInput) {
                deleteInput = document.createElement('input');
                deleteInput.type = 'hidden';
                deleteInput.name = 'deleteImageIdsString';
                form.appendChild(deleteInput);
            }
            deleteInput.value = deleteExistingImageIds.join(",");
        });
    }
});
