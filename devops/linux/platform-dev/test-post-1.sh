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
    "div" : "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p class=\"res-header-id\"><b>Generated Narrative: Patient SampleSzPatient</b></p><a name=\"SampleSzPatient\"> </a><a name=\"hcSampleSzPatient\"> </a><a name=\"SampleSzPatient-en-US\"> </a><p style=\"border: 1px #661aff solid; background-color: #e6e6ff; padding: 10px;\">Celucolo Celani Sacolo  Male, DoB: 2000-01-01 ( Medical Record Number: M001010101-1\u00a0(use:\u00a0usual,\u00a0))</p><hr/><table class=\"grid\"><tr><td style=\"background-color: #f3f5da\" title=\"Other Id (see the one above)\">Other Id:</td><td colspan=\"3\">Personal ID Number/2001010000001\u00a0(use:\u00a0official,\u00a0)</td></tr><tr><td style=\"background-color: #f3f5da\" title=\"Ways to contact the Patient\">Contact Detail</td><td colspan=\"3\"><ul><li>-unknown-(Mobile)</li><li>ph: 7600 0000</li><li>123 Somhlolo Rd Mbabane 0000 SZ </li></ul></td></tr><tr><td style=\"background-color: #f3f5da\" title=\"Language spoken\">Language:</td><td colspan=\"3\"><span title=\"Codes:{urn:ietf:bcp:47 en-GB}\">English</span></td></tr><tr><td style=\"background-color: #f3f5da\" title=\"The nationality of the patient.\">Patient Nationality:</td><td colspan=\"3\"><ul><li>code: <span title=\"Codes:{urn:iso:std:iso:3166 SWZ}\">Eswatini</span></li><li>period: 1981-01-20 --&gt; (ongoing)</li></ul></td></tr><tr><td style=\"background-color: #f3f5da\" title=\"Base StructureDefinition for code type: A string which has at least one character and no leading or trailing whitespace and where there is no whitespace other than single spaces in the contents\"><a href=\"http://hl7.org/fhir/R4/datatypes.html#code\"/></td><td colspan=\"3\"><ul><li><span title=\"Codes:{http://localhost:3447/fhir/CodeSystem/SzTinkhundlaCS DVOKODVWENI}\">Dvokodvweni</span></li><li><span title=\"Codes:{http://localhost:3447/fhir/CodeSystem/SzChiefdomCS Kwaluseni}\">Kwaluseni</span></li></ul></td></tr></table></div>"
  },
  "extension" : [
    {
      "extension" : [
        {
          "url" : "code",
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
          "url" : "period",
          "valuePeriod" : {
            "start" : "1981-01-20"
          }
        }
      ],
      "url" : "http://hl7.org/fhir/StructureDefinition/patient-nationality"
    },
    {
      "url" : "code",
      "valueCodeableConcept" : {
        "coding" : [
          {
            "system" : "http://localhost:3447/fhir/CodeSystem/SzTinkhundlaCS",
            "code" : "DVOKODVWENI",
            "display" : "Dvokodvweni"
          }
        ],
        "text" : "Dvokodvweni"
      }
    },
    {
      "url" : "code",
      "valueCodeableConcept" : {
        "coding" : [
          {
            "system" : "http://localhost:3447/fhir/CodeSystem/SzChiefdomCS",
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
      "system" : "http://mfl.sys/m001",
      "value" : "M001010101-1"
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
      "system" : "http://homeaffairs.sys",
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
      "use" : "mobile"
    },
    {
      "system" : "phone",
      "value" : "7600 0000",
      "rank" : 1
    }
  ],
  "gender" : "male",
  "birthDate" : "2000-01-01",
  "_birthDate" : {
    "extension" : [
      {
        "url" : "http://hl7.org/fhir/StructureDefinition/patient-birthTime",
        "valueDateTime" : "2000-01-01T14:35:45-05:00"
      }
    ]
  },
  "address" : [
    {
      "line" : [
        "123 Somhlolo Rd"
      ],
      "city" : "Mbabane",
      "postalCode" : "0000",
      "country" : "SZ"
    }
  ],
  "communication" : [
    {
      "language" : {
        "coding" : [
          {
            "system" : "urn:ietf:bcp:47",
            "code" : "en-GB",
            "display" : "English (Region=Great Britain)"
          }
        ],
        "text" : "English"
      }
    }
  ]
}'
