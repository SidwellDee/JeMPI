#!/bin/bash
set -e
set -m


curl --location 'http://localhost:5001/fhir/Patient' \
--header 'Authorization: custom test' \
--header 'Content-Type: application/fhir+json' \
--data '
{
  "resourceType" : "Patient",
  "id" : "SampleSzPatient",
  "meta" : {
    "profile" : [
      "http://192.168.10.200:3447/fhir/StructureDefinition/SzPatient"
    ]
  },
  "text" : {
    "status" : "generated",
    "div" : "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p class=\"res-header-id\"><b>Generated Narrative: Patient SampleSzPatient</b></p><a name=\"SampleSzPatient\"> </a><a name=\"hcSampleSzPatient\"> </a><a name=\"SampleSzPatient-en-US\"> </a><p style=\"border: 1px #661aff solid; background-color: #e6e6ff; padding: 10px;\">Celucolo Celani Sacolo  Male, DoB: 2000-01-01 ( Medical Record Number: Client Management Information System#M001010101-1\u00a0(use:\u00a0usual,\u00a0))</p><hr/><table class=\"grid\"><tr><td style=\"background-color: #f3f5da\" title=\"Other Id (see the one above)\">Other Id:</td><td colspan=\"3\">Personal ID Number/2001010000001\u00a0(use:\u00a0official,\u00a0)</td></tr><tr><td style=\"background-color: #f3f5da\" title=\"Ways to contact the Patient\">Contact Detail</td><td colspan=\"3\"><ul><li>ph: 7600 0000</li><li>123 Somhlolo Rd Mbabane 0000 SZ </li></ul></td></tr><tr><td style=\"background-color: #f3f5da\" title=\"Extention for Eswatini Tinkhundla\"><a href=\"StructureDefinition-SzInkhundlaExtension.html\">Extention: Eswatini Inkhundla</a></td><td colspan=\"3\"><span title=\"Codes:{http://192.168.10.200:3447/fhir/CodeSystem/SzTinkhundlaCS DVOKODVWENI}\">Dvokodvweni</span></td></tr><tr><td style=\"background-color: #f3f5da\" title=\"The nationality of the patient.\"><a href=\"http://hl7.org/fhir/extensions/5.1.0/StructureDefinition-patient-nationality.html\">Patient Nationality</a></td><td colspan=\"3\"><span title=\"Codes:{urn:iso:std:iso:3166 SWZ}\">Eswatini</span></td></tr><tr><td style=\"background-color: #f3f5da\" title=\"Extention for Eswatini Chiefdom\"><a href=\"StructureDefinition-SzChiefdomExtension.html\">Extention: Eswatini Chiefdom</a></td><td colspan=\"3\"><span title=\"Codes:{http://192.168.10.200:3447/fhir/CodeSystem/SzChiefdomCS Kwaluseni}\">Kwaluseni</span></td></tr></table></div>"
  },
  "extension" : [
    {
      "url" : "http://hl7.org/fhir/StructureDefinition/patient-nationality",
      "valueCodeableConcept" : {
        "coding" : [
          {
            "system" : "urn:iso:std:iso:3166",
            "code" : "SWZ",
            "display" : "Eswatini"
          }
        ]
      }
    },
    {
      "url" : "http://192.168.10.200:3447/fhir/StructureDefinition/SzInkhundlaExtension",
      "valueCodeableConcept" : {
        "coding" : [
          {
            "system" : "http://192.168.10.200:3447/fhir/CodeSystem/SzTinkhundlaCS",
            "code" : "DVOKODVWENI",
            "display" : "Dvokodvweni"
          }
        ],
        "text" : "Dvokodvweni"
      }
    },
    {
      "url" : "http://192.168.10.200:3447/fhir/StructureDefinition/SzChiefdomExtension",
      "valueCodeableConcept" : {
        "coding" : [
          {
            "system" : "http://192.168.10.200:3447/fhir/CodeSystem/SzChiefdomCS",
            "code" : "Kwaluseni",
            "display" : "Kwaluseni"
          }
        ],
        "text" : "Kwaluseni"
      }
    }
  ],
  "identifier" : [
    {
      "use" : "usual",
      "type" : {
        "coding" : [
          {
            "system" : "http://192.168.10.200:3447/fhir/CodeSystem/SzPersonIdentificationsCS",
            "code" : "MR",
            "display" : "Medical Record Number"
          }
        ]
      },
      "system" : "urn:health:cbhis",
      "value" : "COMM01-11-22"
    },
    {
      "use" : "official",
      "type" : {
        "coding" : [
          {
            "system" : "http://192.168.10.200:3447/fhir/CodeSystem/SzPersonIdentificationsCS",
            "code" : "PI",
            "display" : "Personal ID Number"
          }
        ]
      },
      "system" : "urn:homeaffairs:population-register",
      "value" : "2001010000001"
    }
  ],
  "name" : [
    {
      "family" : "Sacolo",
      "given" : [
        "Celucolo",
        "Celani"
      ]
    }
  ],
  "telecom" : [
    {
      "system" : "phone",
      "value" : "7600 0000"
    }
  ],
  "gender" : "male",
  "birthDate" : "2000-01-01",
  "address" : [
    {
      "line" : [
        "123 Somhlolo Rd"
      ],
      "city" : "Mbabane",
      "postalCode" : "0000",
      "country" : "SZ"
    }
  ]
}'
