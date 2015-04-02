<form id="requestForm" method="post" class="form-horizontal" action="">
	<div class="form-group">
		<label class="col-lg-2 control-label">Service URL</label>
		<div class="col-lg-10">
			<input class="form-control" name="url" id="serviceUrlField" value="./WebProcessingService" type="text" />
		</div>
	</div>
	<div class="form-group">
		<label class="col-lg-2 control-label">Request Examples</label>
		<div class="col-lg-10">
			<select id="selRequest" class="form-control">
				<option value=" "></option>
			</select>
		</div>
	</div>
	<div class="form-group">
		<pre class="editor"><textarea name="request" id="requestTextarea" class="form-control"></textarea></pre>
	</div>
	<div class="form-group">
		<button type="submit" class="btn btn-primary">Send</button>
		<button id="clearBtn" type="reset" class="btn btn-primary">Clear</button>
	</div>
</form>
<pre><textarea name="request" id="responseTextarea"></textarea></pre>

<script src="static/js/codemirror/codemirror.js" type="text/javascript"></script>

<script type="text/javascript">
	$(document).ready(
			function() {
				// derive service url from current location
				var urlIndex = window.location.href.lastIndexOf("/test_client");
				var urlBasisString = window.location.href.substring(0, (urlIndex + 1));
				var serviceUrlString = urlBasisString + "WebProcessingService";

				var datafolder = window.location.href.substring(0, window.location.href.lastIndexOf("/") + 1)
						+ "static/examples/requests/";

				initEditors();

				var placeholderIndex = "PLACEHOLDER";
				//load files
				var requests = new Array();
				requests[100] = datafolder + "GetCapabilities.xml";
				requests[101] = datafolder + "DescribeProcess.xml";

				requests[120] = datafolder + "/2.0.0/GetCapabilitiesRequest200.xml";
				requests[121] = datafolder + "/2.0.0/DescribeProcessRequest200.xml";
				requests[122] = datafolder + "/2.0.0/SimpleBufferExecute200.xml";
				requests[122] = datafolder + "/2.0.0/FloodEnrichmentExecute200.xml";
				requests[123] = datafolder + "SimpleBuffer.xml";
				requests[124] = datafolder + "Intersectionrequest.xml";
				requests[125] = datafolder + "Unionrequest.xml";

				//fill the select element
				var selRequest = $('#selRequest');

				l = requests.length;
				for ( var i = 0; i < l; i++) {
					var requestString = "";
					if (requests[i] == placeholderIndex) {
						//skip this one
					} else if (requests[i]) {
						try {
							var name = requests[i].substring(requests[i].lastIndexOf("/") + 1, requests[i].length);
							selRequest.append($("<option></option>").attr("value", requests[i]).text(name));
						} catch (err) {
							var txt = "";
							txt += "Error loading file: " + requests[i];
							txt += "Error: " + err + "\n\n";
							var requestTextarea = document.getElementById('requestTextarea').value = "";
							requestTextarea.value += txt;
						}
					} else {
						// request is null or empty string - do nothing
					}
				}

				// Put service url into service url field
				var serviceUrlField = document.getElementById("serviceUrlField");
				serviceUrlField.value = serviceUrlString;

				$('form#requestForm').submit(function(event) {
					event.preventDefault();
					var form = $(this);
					var requestTextareaValue = $('#requestTextarea').val();
					$.ajax({
						type : form.attr('method'),
						url : $("#requestForm input[name=url]").val(),
						data : requestTextareaValue,
						complete : function(xhr) {
							outputEditor.setCode(xhr.responseText);
						}
					});
				});

				$('#selRequest').change(function() {
					try {
						var selObj = $(this);
						var requestTextarea = $('#requestTextarea');
						var requestString = "";

						if ($('#selRequest').prop('selectedIndex') != 0) { // Handle selection of empty drop down entry.
							requestString = getFile(selObj.val());
						}

						if (requestString == null) {
							requestString = "Sorry! There is a problem, please refresh the page.";
						}

						inputEditor.setCode(requestString);

					} catch (err) {
						var txt = "";
						txt += "Error loading file: " + selObj.val();
						txt += "Error: " + err + "\n\n";
						requestTextarea.value += txt;
					}
				});
				
				$('#clearBtn').click(function(event){ 
					event.preventDefault();
					inputEditor.setCode('');
					outputEditor.setCode('');
					$('#selRequest').prop('selectedIndex', 0);
				});
			});

	function getFile(fileName) {
		oxmlhttp = null;
		try {
			oxmlhttp = new XMLHttpRequest();
			oxmlhttp.overrideMimeType("text/xml");
		} catch (e) {
			try {
				oxmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
			} catch (e) {
				return null;
			}
		}
		if (!oxmlhttp)
			return null;
		try {
			oxmlhttp.open("GET", fileName, false);
			oxmlhttp.send(null);
		} catch (e) {
			return null;
		}
		return oxmlhttp.responseText;
	}

	function initEditors() {
		var defaultInputString = "<!-- Insert your request here or select one of the examples from the menu above. -->";
		var defaultOutputString = "<!-- Output -->";

		inputEditor = CodeMirror.fromTextArea("requestTextarea", {
			height : "300px",
			parserfile : "parsexml.js",
			stylesheet : "static/js/codemirror/xmlcolors.css",
			path : "static/js/codemirror/",
			lineNumbers : true,
			content : defaultInputString
		});

		outputEditor = CodeMirror.fromTextArea("responseTextarea", {
			height : "300px",
			parserfile : "parsexml.js",
			stylesheet : "static/js/codemirror/xmlcolors.css",
			path : "static/js/codemirror/",
			lineNumbers : true,
			readOnly : true,
			content : defaultOutputString
		});
	}
</script>