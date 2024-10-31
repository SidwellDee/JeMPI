#!/bin/bash
set -e
set -m


curl --location 'http://localhost:50000/JeMPI/crLink' \
--header 'Content-Type: application/json' \
--data '{
    "matchThreshold": 0.9,
    "sourceId": {
        "facility": "fac1",
        "patient": "pat1"
    },
    "uniqueInteractionData": {
        "auxDateCreated": "2016-10-30T14:22:25.285Z",
        "auxId": "rec-0000000001-01",
        "auxClinicalData": "RANDOM DATA"
    },
    "demographicData": {
	"pin": "pin-1234",
        "firstName": "John",
        "middleName": "bbb",
	"surname": "Doe",
	"sex": "maleeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee",
        "dob": "20000202",
	"birthTime": "00:00:00",
	"cellPhone": "7661112",
	"inkhundla": "Maseyisini",
	"chiefdom": "Shiselweni II",
	"nationality": "Kingdom of Eswatini",
        "city": "Mbabane"
    }
}
'
