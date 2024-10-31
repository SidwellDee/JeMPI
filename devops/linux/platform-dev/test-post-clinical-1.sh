#!/bin/bash
set -e
set -m


curl --location 'http://localhost:5001/fhir/' \
--header 'Authorization: custom test' \
--header 'Content-Type: application/fhir+json' \
--data '{
   "resourceType":"Bundle",
   "id":"SampleSzBundle",
   "type":"transaction",
   "entry":[
        {
            "fullUrl": "http://localhost:3447/DocumentReference/SampleSzPatient1",
            "resource":{
                    "resourceType": "Patient",
                    "id": "SampleSzPatient1",
                    "meta": {
                        "profile": [
                        "http://localhost:3447/fhir/StructureDefinition/SzPatient"
                        ]
                    },
                    "extension": [
                        {
                        "url": "http://hl7.org/fhir/StructureDefinition/patient-nationality",
                        "extension": [
                            {
                            "url": "code",
                            "valueCodeableConcept": {
                                "coding": [
                                {
                                    "code": "SWZ",
                                    "system": "urn:iso:std:iso:3166",
                                    "display": "Eswatini"
                                }
                                ]
                            }
                            },
                            {
                            "url": "period",
                            "valuePeriod": {
                                "start": "1981-01-20"
                            }
                            }
                        ]
                        }
                    ],
                    "name": [
                        {
                        "family": "Grealish",
                        "given": [
                            "Jack",
                            "John"
                        ]
                        }
                    ],
                    "telecom": [
                        {
                        "use": "mobile"
                        },
                        {
                        "system": "phone",
                        "value": "7600 3333",
                        "rank": 1
                        }
                    ],
                    "identifier": [
                        {
                        "use": "usual",
                        "type": {
                            "coding": [
                            {
                                "system": "http://localhost:3447/fhir/CodeSystem/SzPersonIdentificationsCS",
                                "code": "MR",
                                "display": "Medical Record Number"
                            }
                            ],
                            "text": "Personal ID Number"
                        },
                        "system": "http://mfl.sys/m001",
                        "value": "M001010101-1"
                        },
                        {
                        "use": "official",
                        "type": {
                            "coding": [
                            {
                                "system": "http://localhost:3447/fhir/CodeSystem/SzPersonIdentificationsCS",
                                "code": "PI",
                                "display": "Personal ID Number"
                            }
                            ]
                        },
                        "system": "http://homeaffairs.sys",
                        "value": "2001010000001"
                        }
                    ],
                    "gender": "male",
                    "birthDate": "2000-01-01",
                    "_birthDate": {
                        "extension": [
                        {
                            "url": "http://hl7.org/fhir/StructureDefinition/patient-birthTime",
                            "valueDateTime": "2000-01-01T14:35:45-05:00"
                        }
                        ]
                    },
                    "address": [
                        {
                        "line": [
                            "123 Somhlolo Rd"
                        ],
                        "city": "Mbabane",
                        "postalCode": "0000",
                        "country": "SZ"
                        }
                    ],
                    "communication": [
                        {
                        "language": {
                            "coding": [
                            {
                                "code": "en-GB",
                                "system": "urn:ietf:bcp:47",
                                "display": "English (Region=Great Britain)"
                            }
                            ],
                            "text": "English"
                        }
                        }
                    ]
                
            },
            "request": {
                "method": "PUT",
                "url": "Patient/SampleSzPatient1"
            }
        },
        {
            "fullUrl":"http://localhost:3447/DocumentReference/SampleSzEncounter1",
            "resource": {
                    "resourceType": "Encounter",
                    "id": "SampleSzEncounter1",
                    "meta": {
                        "profile": [
                        "http://localhost:3447/fhir/StructureDefinition/SzEncounter"
                        ]
                    },
                    "status": "finished",
                    "class": {
                        "system": "http://localhost:3447/fhir/CodeSystem/SzEncounterClassificationCS",
                        "code": "OPD",
                        "display": "Out Patient Department"
                    },
                    "reasonCode": [
                        {
                        "coding": [
                            {
                            "system": "http://localhost:3447/fhir/CodeSystem/SzServicePointCS",
                            "code": "HTS",
                            "display": "HIV Testing Services"
                            }
                        ],
                        "text": "HIV Testing Services"
                        }
                    ],
                    "location": [
                        {
                        "location": {
                            "reference": "Location/SampleSzLocation"
                        },
                        "status": "completed"
                        }
                    ],
                    "participant": [
                        {
                        "individual": {
                            "reference": "Practitioner/SampleSzPractitioner"
                        }
                        }
                    ],
                    "subject": {
                        "reference": "Patient/SampleSzPatient1"
                    }
            },
            "request": {
                "method": "PUT",
                "url": "Encounter/SampleSzEncounter1"
            }
        }
    ]
}'
