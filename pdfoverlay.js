
var updater_po = null;

function startStatusCheck_po()
{
	updater_po = null;
	try
	{
		document.getElementById("messageDiv").innerHTML = "";
		updater_po = new Ajax.PeriodicalUpdater(
				'messageDiv',
				'UploadStatusPDFpo?sid=' + Math.random(),
			{
				asynchronous : true,
				frequency : 1,
				method : 'get',
				cache : false,
				onFailure : function ()
				{
					document.getElementById("messageStatusPO").innerHTMLalert = "Error : server communication. Please try later."
				},
				onSuccess : function (request)
				{
					if (request.responseText.length > 1)
					{
						var xml = request.responseXML;

						if (xml != null)
						{

							var bytesProcessed = xml.getElementsByTagName("bytesProcessed")[0];
							var sizeTotal = xml.getElementsByTagName("sizeTotal")[0];
							var speed = xml.getElementsByTagName("speed")[0];
							var timeLeft = xml.getElementsByTagName("timeLeft")[0];
							var status = xml.getElementsByTagName("status")[0];
							var errormsg = xml.getElementsByTagName("errormsg")[0];
							var directory = xml.getElementsByTagName("directory")[0];
							var filename = xml.getElementsByTagName("filename")[0];
							var n = xml.getElementsByTagName("n")[0];

							bytesProcessed = bytesProcessed.firstChild.data;
							sizeTotal = sizeTotal.firstChild.data;
							speed = speed.firstChild.data;
							timeLeft = timeLeft.firstChild.data;
							status = status.firstChild.data;
							directory = directory.firstChild.data;
							filename = filename.firstChild.data;
							n = n.firstChild.data;
							
							document.getElementById("messageDiv").innerHTML = "";

							if (status == "PROCESSED")
							{
								killUpdate_po("", directory, filename, n);
							}
							else if (status == "ERROR")
							{
								errormsg = errormsg.firstChild.data;
								killUpdate_po(errormsg, "", "", 0);
							}
							else
							{
								if (bytesProcessed == sizeTotal)
								{
									document.getElementById("messageStatusPO").innerHTML = (sizeTotal / 1024 / 1024).toFixed(2) + " MB (100%)";
								}
								else
								{
									var myPercent = ((100 * bytesProcessed) / sizeTotal).toFixed(0);
									document.getElementById("messageStatusPO").innerHTML = (bytesProcessed / 1024 / 1024).toFixed(2) + " MB / " + (sizeTotal / 1024 / 1024).toFixed(2) + " MB (" + myPercent + "%)";
								}
							}
						}
						else
						{
							errormsg = "FATAL ERROR!";
							killUpdate_po(errormsg, "", "", 0);
						}
					}
				}
			}
			);
		return true;
	}
	catch (e)
	{
		document.getElementById("messageStatusPO").innerHTML = "ERROR (1) : " + e.toString();
	}
}
/* ========================================================================== */
function killUpdate_po(m, d, f, n)
{
	if (m != "")
	{
		document.getElementById("messageStatusPO").innerHTML = "ERROR (2) : " + m;
		updater_po.stop();
		updater_po = null;
	}
	else
	{
		if (d)
		{
			document.getElementById("messageStatusPO").innerHTML = "File : <a href='temp/PDFpo/" + d + "/" + f + "' target='_blank'>" + f + "</a>";

			updater_po.stop();
			updater_po = null;
		}
	}
}

/* ============================= File upload ================================ */
function upload_file_po(v)
{
	if (updater_po != null)
	{
		document.getElementById("messageStatusPO").innerHTML = "ERRO (3) : Wait, still processing...";
	}
	else if (v.length > 0)
	{
		document.getElementById("messageStatusPO").innerHTML = "";
		document["upload_po"].m.value = document.getElementById("pdf_model_po").options[document.getElementById("pdf_model_po").selectedIndex].value;
		document["upload_po"].submit();
		startStatusCheck_po();
	}
}
/* ========================================================================== */
