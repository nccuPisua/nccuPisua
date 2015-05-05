(function () {
    $(function(){
        $('.facebook').on('click', login);
    });
    
    function login () {
        Parse.FacebookUtils.logIn('public_profile, email, user_birthday, user_friends', {
            success: success,
            error: error
        });

    }
	
})()