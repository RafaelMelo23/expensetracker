document.getElementById('registrationForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const form = event.target;
    const data = {
        firstName: form.firstName.value,
        lastName: form.lastName.value,
        email: form.email.value,
        password: form.password.value
    };


    document.querySelectorAll('.error').forEach(function(error) {
        error.textContent = '';
    });
    document.getElementById('responseMessage').textContent = '';
    document.getElementById('responseMessage').className = '';


    let isValid = true;
    if (!data.firstName || data.firstName.length > 30) {
        document.getElementById('firstNameError').textContent = 'First name is invalid.';
        isValid = false;
    }
    if (!data.lastName || data.lastName.length > 50) {
        document.getElementById('lastNameError').textContent = 'Last name is invalid.';
        isValid = false;
    }
    if (!data.email || !/\S+@\S+\.\S+/.test(data.email)) {
        document.getElementById('emailError').textContent = 'Invalid email address.';
        isValid = false;
    }
    if (!data.password || data.password.length < 12 || data.password.length > 72) {
        document.getElementById('passwordError').textContent = 'Password must be between 12 and 72 characters.';
        isValid = false;
    }

    const confirmPassword = document.getElementById('confirmPassword').value;
    if (data.password !== confirmPassword) {
        document.getElementById('confirmPasswordError').textContent = 'Passwords do not match.';
        isValid = false;
    }

    if (!isValid) return;


    fetch('/api/user/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => {
            if (response.status === 201) {
                document.getElementById('responseMessage').textContent = 'Registration successful! You can now log in.';
                document.getElementById('responseMessage').classList.add('success');
                form.reset(); // Reset form after successful registration
            } else {
                response.json().then(error => {
                    document.getElementById('responseMessage').textContent = 'Error: ' + error.message;
                    document.getElementById('responseMessage').classList.add('error-response');
                });
            }
        })
        .catch(error => {
            document.getElementById('responseMessage').textContent = 'Error registering user: ' + error.message;
            document.getElementById('responseMessage').classList.add('error-response');
        });
});