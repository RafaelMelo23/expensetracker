document.getElementById('loginForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const form = event.target;
    const data = {
        email: form.email.value,
        password: form.password.value
    };

    document.querySelectorAll('.error').forEach(function(error) {
        error.textContent = '';
    });
    document.getElementById('responseMessage').textContent = '';
    document.getElementById('responseMessage').className = '';

    let isValid = true;
    if (!data.email || !/\S+@\S+\.\S+/.test(data.email)) {
        document.getElementById('emailError').textContent = 'Invalid email address.';
        isValid = false;
    }
    if (!data.password || data.password.length < 7 || data.password.length > 72) {
        document.getElementById('passwordError').textContent = 'Password must be between 12 and 72 characters.';
        isValid = false;
    }

    if (!isValid) return;

    fetch('/api/user/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => {
            if (response.ok) {
                window.location.href = '/dashboard';
            } else {
                document.getElementById('responseMessage').textContent = 'Invalid email or password.';
                document.getElementById('responseMessage').classList.add('error-response');
            }
        })
        .catch(error => {
            document.getElementById('responseMessage').textContent = 'Error logging in: ' + error.message;
            document.getElementById('responseMessage').classList.add('error-response');
        });
});