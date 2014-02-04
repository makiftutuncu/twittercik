$(document).ready(function()
{
    $('#tweetcik').value = "";
    $('#tweetcik').trigger('input');
});

function ensureTweetcikLength(textarea)
{
    var length = textarea.value.length;
    var remaining = 140 - length;

    if(remaining < 0 || remaining == 140)
    {
        $('#tweetcik-button').prop('disabled', true);
    }
    else
    {
        $('#tweetcik-button').prop('disabled', false);
    }

    $('#tweetcik-counter').html(remaining);
};