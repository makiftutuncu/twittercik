function validateForm(form, username, password)
{
    // Check validity of form fields
    re = /^\w+$/;

    if(username.value == '' || password.value == '')
    {
        alert("Username and password cannot be empty.");
        username.focus();

        return false;
    }
    else if(username.value.length < 3 || username.value.length > 24)
    {
        alert("Username should be 3-24 characters long.");
        username.focus();

        return false;
    }
    else if(!re.test(form.username.value))
    { 
        alert("Username must contain only letters, numbers and underscores.");
        username.focus();

        return false; 
    }
    else if(password.value.length < 6 || password.value.length > 32)
    {
        alert("Password should be 6-32 characters long.");
        password.focus();
        
        return false;
    }

    // Create a new element input, this will be our hashed password field. 
    var hashedPassword = document.createElement("input");
 
    // Add the new element to our form. 
    hashedPassword.name = "hashedpassword";
    hashedPassword.type = "hidden";
    hashedPassword.value = hex_sha512(password.value);
    form.appendChild(hashedPassword);
 
    // Make sure the plaintext password doesn't get sent. 
    password.value = "";
 
    // Finally submit the form. 
    form.submit();

    return true;
}