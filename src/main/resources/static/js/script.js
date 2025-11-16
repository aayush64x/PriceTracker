document.addEventListener('DOMContentLoaded', () => {
    // Select all "Add to Watchlist" buttons
    const watchlistButtons = document.querySelectorAll('.product-card button');
    const modal = document.getElementById('watchlistModal');
    const asinField = document.getElementById('asinField');
    const closeModal = () => modal.style.display = 'none';

    // Open modal when button is clicked
    watchlistButtons.forEach(button => {
        button.addEventListener('click', (event) => {
            const productCard = event.target.closest('.product-card');
            const asin = productCard.querySelector('span').innerText; // ASIN span inside product card

            asinField.value = asin;   // set hidden input value
            modal.style.display = 'block';
        });
    });

    // Close modal if user clicks outside the form
    window.addEventListener('click', (event) => {
        if (event.target === modal) {
            closeModal();
        }
    });

    // Optional: handle form submit with AJAX
    const form = modal.querySelector('form');
    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const formData = new FormData(form);
        const data = {
            asin: formData.get('asin'),
            email: formData.get('email'),
            targetPrice: parseFloat(formData.get('targetPrice'))
        };

        try {
            const response = await fetch(form.action, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            if (response.ok) {
                alert('Product added to watchlist!');
                closeModal();
            } else {
                alert('Failed to add product. Please try again.');
            }
        } catch (error) {
            console.error('Error adding to watchlist:', error);
            alert('Error occurred. Check console.');
        }
    });
});