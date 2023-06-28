function checkGT(a,b){
	print("function checkGT invoked");
	
	if ( Number(a) > Number(b))
		return "true";
	else
		return "false";
}
function checkEQ(a,b){
	print("function checkEQ invoked");
	
	if ( Number(a) == Number(b))
		return "true";
	else
		return "false";
}
function checkForNull(a){
    print("function checkForNull is invoked");
    if (a == null )
        return "true";
    else
        return "false";
}

function matchString(a,b){
	print("function matchString invoked");
	
	if ( a == b)
		return "true";
	else
		return "false";
}
function contains(a,b){
	print("function contains invoked");
	var str1 = a.toString();
	var str2 = b.toString();
	print(str1);
	print(str2);
	if ( str1.contains(str2) )
		return "true";
	else
		return "false";
}
function concatenate(a,b){
	print("function concatenate invoked");
	var str1 = a.toString();
	var str2 = b.toString();
	print(str1);
	print(str2);
	return str1 + str2;
	
}

function decrement(a){
	print("function decrement invoked"+a);
	
	var x =  Number(a) - 1;
	print(x);
	return x.toString();
	
}
function increment(a){
	print("function increment invoked"+a);
	
	var x =  Number(a) + 1;
	print(x);
	return x.toString();
	
}

function add(a,b){
	print("function add invoked");
	
	var x =  Number(a) + Number(b);
	print(x);
	return x.toString();
	
}

function sub(a,b){
	print("function sub invoked");
	
	var x =  Number(a) - Number(b);
	print(x);
	return x.toString();
	
}


function helloTran(stmt1,stmt2,stmt3) {
   print("Yur stmt 1 is "+stmt1);
   print("Yur stmt 2 is "+stmt2);
   print("Yur stmt 3 is "+stmt3);
   var oneperson = javainterface.getTranAttribute(engine,"nextperson");
   var whatsay=oneperson.learn();
   print("From script["+whatsay+"]");
   //alert(aperson.grow());
   print("Person:"+oneperson.age);

}

function helloInstance(stmt1,stmt2,stmt3) {
   print("Yur stmt 1 is "+stmt1);
   print("Yur stmt 2 is "+stmt2);
   print("Yur stmt 3 is "+stmt3);
   var oneperson = javainterface.getInstanceAttribute(engine,"aperson");
   var whatsay=oneperson.learn();
   print("From script["+whatsay+"]");
   //alert(aperson.grow());
   print("Person:"+oneperson.age);

}

function  simpleInt(P,R,T) {
   print("Principal:"+P+" Rate:"+R+" Time:"+T);
   simple = (P*R*T)/100;
   print("Simple Interest["+simple+"]");
   return simple;

}


