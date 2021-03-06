package org.hl7.fhir.r4.model.codesystems;

/*
  Copyright (c) 2011+, HL7, Inc.
  All rights reserved.
  
  Redistribution and use in source and binary forms, with or without modification, 
  are permitted provided that the following conditions are met:
  
   * Redistributions of source code must retain the above copyright notice, this 
     list of conditions and the following disclaimer.
   * Redistributions in binary form must reproduce the above copyright notice, 
     this list of conditions and the following disclaimer in the documentation 
     and/or other materials provided with the distribution.
   * Neither the name of HL7 nor the names of its contributors may be used to 
     endorse or promote products derived from this software without specific 
     prior written permission.
  
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  POSSIBILITY OF SUCH DAMAGE.
  
*/

// Generated on Tue, Jan 9, 2018 14:51-0500 for FHIR v3.2.0


import org.hl7.fhir.exceptions.FHIRException;

public enum MessageEvents {

        /**
         * Change the status of a Medication Administration to show that it is complete.
         */
        MEDICATIONADMINISTRATIONCOMPLETE, 
        /**
         * Someone wishes to record that the record of administration of a medication is in error and should be ignored.
         */
        MEDICATIONADMINISTRATIONNULLIFICATION, 
        /**
         * Indicates that a medication has been recorded against the patient's record.
         */
        MEDICATIONADMINISTRATIONRECORDING, 
        /**
         * Update a Medication Administration record.
         */
        MEDICATIONADMINISTRATIONUPDATE, 
        /**
         * Notification.
         */
        ADMINNOTIFY, 
        /**
         * The definition of a code system is used to create a simple collection of codes suitable for use for data entry or validation. An expanded code system will be returned, or an error message.
         */
        CODESYSTEMEXPAND, 
        /**
         * Notification to convey information.
         */
        COMMUNICATIONREQUEST, 
        /**
         * Provide a diagnostic report, or update a previously provided diagnostic report.
         */
        DIAGNOSTICREPORTPROVIDE, 
        /**
         * Provide a simple observation or update a previously provided simple observation.
         */
        OBSERVATIONPROVIDE, 
        /**
         * Notification that two patient records actually identify the same patient.
         */
        PATIENTLINK, 
        /**
         * Notification that previous advice that two patient records concern the same patient is now considered incorrect.
         */
        PATIENTUNLINK, 
        /**
         * The definition of a value set is used to create a simple collection of codes suitable for use for data entry or validation. An expanded value set will be returned, or an error message.
         */
        VALUESETEXPAND, 
        /**
         * added to help the parsers
         */
        NULL;
        public static MessageEvents fromCode(String codeString) throws FHIRException {
            if (codeString == null || "".equals(codeString))
                return null;
        if ("MedicationAdministration-Complete".equals(codeString))
          return MEDICATIONADMINISTRATIONCOMPLETE;
        if ("MedicationAdministration-Nullification".equals(codeString))
          return MEDICATIONADMINISTRATIONNULLIFICATION;
        if ("MedicationAdministration-Recording".equals(codeString))
          return MEDICATIONADMINISTRATIONRECORDING;
        if ("MedicationAdministration-Update".equals(codeString))
          return MEDICATIONADMINISTRATIONUPDATE;
        if ("admin-notify".equals(codeString))
          return ADMINNOTIFY;
        if ("codesystem-expand".equals(codeString))
          return CODESYSTEMEXPAND;
        if ("communication-request".equals(codeString))
          return COMMUNICATIONREQUEST;
        if ("diagnosticreport-provide".equals(codeString))
          return DIAGNOSTICREPORTPROVIDE;
        if ("observation-provide".equals(codeString))
          return OBSERVATIONPROVIDE;
        if ("patient-link".equals(codeString))
          return PATIENTLINK;
        if ("patient-unlink".equals(codeString))
          return PATIENTUNLINK;
        if ("valueset-expand".equals(codeString))
          return VALUESETEXPAND;
        throw new FHIRException("Unknown MessageEvents code '"+codeString+"'");
        }
        public String toCode() {
          switch (this) {
            case MEDICATIONADMINISTRATIONCOMPLETE: return "MedicationAdministration-Complete";
            case MEDICATIONADMINISTRATIONNULLIFICATION: return "MedicationAdministration-Nullification";
            case MEDICATIONADMINISTRATIONRECORDING: return "MedicationAdministration-Recording";
            case MEDICATIONADMINISTRATIONUPDATE: return "MedicationAdministration-Update";
            case ADMINNOTIFY: return "admin-notify";
            case CODESYSTEMEXPAND: return "codesystem-expand";
            case COMMUNICATIONREQUEST: return "communication-request";
            case DIAGNOSTICREPORTPROVIDE: return "diagnosticreport-provide";
            case OBSERVATIONPROVIDE: return "observation-provide";
            case PATIENTLINK: return "patient-link";
            case PATIENTUNLINK: return "patient-unlink";
            case VALUESETEXPAND: return "valueset-expand";
            default: return "?";
          }
        }
        public String getSystem() {
          return "http://hl7.org/fhir/message-events";
        }
        public String getDefinition() {
          switch (this) {
            case MEDICATIONADMINISTRATIONCOMPLETE: return "Change the status of a Medication Administration to show that it is complete.";
            case MEDICATIONADMINISTRATIONNULLIFICATION: return "Someone wishes to record that the record of administration of a medication is in error and should be ignored.";
            case MEDICATIONADMINISTRATIONRECORDING: return "Indicates that a medication has been recorded against the patient's record.";
            case MEDICATIONADMINISTRATIONUPDATE: return "Update a Medication Administration record.";
            case ADMINNOTIFY: return "Notification.";
            case CODESYSTEMEXPAND: return "The definition of a code system is used to create a simple collection of codes suitable for use for data entry or validation. An expanded code system will be returned, or an error message.";
            case COMMUNICATIONREQUEST: return "Notification to convey information.";
            case DIAGNOSTICREPORTPROVIDE: return "Provide a diagnostic report, or update a previously provided diagnostic report.";
            case OBSERVATIONPROVIDE: return "Provide a simple observation or update a previously provided simple observation.";
            case PATIENTLINK: return "Notification that two patient records actually identify the same patient.";
            case PATIENTUNLINK: return "Notification that previous advice that two patient records concern the same patient is now considered incorrect.";
            case VALUESETEXPAND: return "The definition of a value set is used to create a simple collection of codes suitable for use for data entry or validation. An expanded value set will be returned, or an error message.";
            default: return "?";
          }
        }
        public String getDisplay() {
          switch (this) {
            case MEDICATIONADMINISTRATIONCOMPLETE: return "MedicationAdministration Complete";
            case MEDICATIONADMINISTRATIONNULLIFICATION: return "MedicationAdministration Nullification";
            case MEDICATIONADMINISTRATIONRECORDING: return "MedicationAdministration Recording";
            case MEDICATIONADMINISTRATIONUPDATE: return "MedicationAdministration Update";
            case ADMINNOTIFY: return "Admin Notify";
            case CODESYSTEMEXPAND: return "Codesystem Expand";
            case COMMUNICATIONREQUEST: return "Communication Request";
            case DIAGNOSTICREPORTPROVIDE: return "Diagnosticreport Provide";
            case OBSERVATIONPROVIDE: return "Observation Provide";
            case PATIENTLINK: return "Patient Link";
            case PATIENTUNLINK: return "Patient Unlink";
            case VALUESETEXPAND: return "Valueset Expand";
            default: return "?";
          }
    }


}

