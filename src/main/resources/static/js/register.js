const form = document.querySelector(".register-form");
const button = document.getElementById("submit-button");

button.addEventListener("click", async () => {
    // Grab values directly from form fields by name
    const User = {
        firstName: form.firstName.value,
        lastName: form.lastName.value,
        email: form.email.value,
        password: form.password.value
    };

    try {
        const response = await fetch("http://localhost:8080/api/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(User)
        });

        if (response.ok) {
            alert("User added successfully");
        } else {
            const errorText = await response.text();
            alert("Registration failed: " + errorText);
        }
    } catch (error) {
        console.error("Error:", error);
        alert("Error occurred. Check console.");
    }

    console.log("button clicked");
});
