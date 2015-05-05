//initialize
	Parse.initialize("eEkyWoV3SAmRZcZKw8mU0AbiTQXneGTUXA8x4pRs", "1Qv7YO6sgyx3VjD9DXpKjdghOzGG488Ifv5QtAba");
	
//func getData
function getData(){
	//get table
	var User = Parse.Object.extend("User");
	var user = new User();
	var query = new Parse.Query(User);
	query.get("V5vOGeYMXR", {
  success: function(user) {
    // The object was retrieved successfully.
	//put data to var
	var name = user.get("username");
	var UUID = user.get("UUID");
	var email = user.get("email");
	//show result
	var div = document.getElementById("textDiv");
	var textContent = "</br>"+"RESULT:"+"</br></br>"+"User Name:&nbsp;"+name+"</br>"+" UUID:&nbsp;"+UUID+"</br>"+" Email:&nbsp;"+email;
	$(div).html(textContent);
  },
  error: function(object, error) {
    // The object was not retrieved successfully.
    // error is a Parse.Error with an error code and description.
	alert("發生錯誤");
  }
});
		}
		
//func findData
function findData(){
	var User = Parse.Object.extend("User");
	var query = new Parse.Query(User);
	var Qtype = document.getElementById("qType").value;
	var qText = document.getElementById("qText").value;
	if (Qtype=="User Name"){
		var qType="username";
	}else if(Qtype=="Email"){
			var qType="email";
	}else if (Qtype=="UUID"){
			var qType="UUID";
	}else{
			alert("Query Type Error");}	
	//query restriction 
	query.contains(qType, qText);
	//query
	if(qText==""){
		alert("Please enter keyword!");
		}else{
	query.find({
 		 success: function(results) {
    		// Do something with the returned Parse.Object values
    		for (var i = 0; i < results.length; i++) {
      		var object = results[i];
      		alert("ID: "+object.id+"  "+Qtype + ' = ' + object.get(qType));
    }
  },
  error: function(error) {
    alert("Error:");
  }
});
		}
	}