// Client-side helpers for the Copyright Complaint Portal.
// Server-side validation is still the source of truth; these checks only
// give faster feedback in the browser.

document.addEventListener("DOMContentLoaded", function () {

    // Reject files larger than data-max-bytes before they are uploaded.
    document.querySelectorAll("input[type=file][data-max-bytes]").forEach(function (input) {
        input.addEventListener("change", function () {
            var maxBytes = parseInt(input.dataset.maxBytes, 10);
            var file = input.files && input.files[0];

            if (file && file.size > maxBytes) {
                input.setCustomValidity("File size must not exceed " + Math.round(maxBytes / 1048576) + " MB.");
            } else {
                input.setCustomValidity("");
            }
            input.reportValidity();
        });
    });

    // Prevent double submission of forms (e.g. two clicks on "Submit Complaint").
    document.querySelectorAll("form").forEach(function (form) {
        form.addEventListener("submit", function () {
            if (!form.checkValidity()) {
                return;
            }
            form.querySelectorAll("button[type=submit]").forEach(function (button) {
                setTimeout(function () { button.disabled = true; }, 0);
            });
        });
    });
});
