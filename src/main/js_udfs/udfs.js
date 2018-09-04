/**
 * A transform which adds a field to the incoming data.
 * @param {string} inJson
 * @return {string} outJson
 */
function transform(inJson) {
    var obj = JSON.parse(inJson);
    obj._LAST_UPDATED_DATE = obj._LAST_UPDATED.split(" ")[0];
    obj._LAST_UPDATED_TIME = obj._LAST_UPDATED.split(" ")[1];
    obj.NOTES = "Additional notes can be found at www.chicago.gov.";
    return JSON.stringify(obj);
}

