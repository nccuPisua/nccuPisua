 // Initialize Parse
 Parse.initialize("eEkyWoV3SAmRZcZKw8mU0AbiTQXneGTUXA8x4pRs", "1Qv7YO6sgyx3VjD9DXpKjdghOzGG488Ifv5QtAba");

 window.fbAsyncInit = function () {
     Parse.FacebookUtils.init({ // this line replaces FB.init({
         appId: '1386469211682363', // Facebook App ID
         status: true, // check Facebook Login status
         cookie: true, // enable cookies to allow Parse to access the session
         xfbml: true, // initialize Facebook social plugins on the page
         version: 'v2.3' // point to the latest Facebook Graph API version
     });

     // Run code after the Facebook SDK is loaded.
 };
 //     window.fbAsyncInit = function () {
 //       FB.init({
 //         appId: '1386469211682363',
 //       xfbml: true,
 //     version: 'v2.3'
 //    });
 //    };

 (function (d, s, id) {
     var js, fjs = d.getElementsByTagName(s)[0];
     if (d.getElementById(id)) {
         return;
     }
     js = d.createElement(s);
     js.id = id;
     js.src = "//connect.facebook.net/en_US/sdk.js";
     fjs.parentNode.insertBefore(js, fjs);
 }(document, 'script', 'facebook-jssdk'));