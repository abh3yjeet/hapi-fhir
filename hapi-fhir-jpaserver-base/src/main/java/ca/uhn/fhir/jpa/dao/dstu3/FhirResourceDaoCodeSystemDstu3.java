package ca.uhn.fhir.jpa.dao.dstu3;

/*
 * #%L
 * HAPI FHIR JPA Server
 * %%
 * Copyright (C) 2014 - 2016 University Health Network
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hl7.fhir.dstu3.hapi.validation.HapiWorkerContext;
import org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain;
import org.hl7.fhir.dstu3.model.CodeSystem;
import org.hl7.fhir.dstu3.model.CodeSystem.CodeSystemContentMode;
import org.hl7.fhir.dstu3.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.hl7.fhir.dstu3.model.ValueSet.ValueSetExpansionContainsComponent;
import org.hl7.fhir.dstu3.terminologies.ValueSetExpander;
import org.hl7.fhir.dstu3.terminologies.ValueSetExpander.ValueSetExpansionOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.jpa.dao.IFhirResourceDaoCodeSystem;
import ca.uhn.fhir.jpa.entity.ResourceTable;
import ca.uhn.fhir.jpa.entity.TermCodeSystemVersion;
import ca.uhn.fhir.jpa.entity.TermConcept;
import ca.uhn.fhir.jpa.term.ITerminologySvc;
import ca.uhn.fhir.jpa.util.LogicUtil;
import ca.uhn.fhir.rest.method.RequestDetails;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;

public class FhirResourceDaoCodeSystemDstu3 extends FhirResourceDaoDstu3<CodeSystem> implements IFhirResourceDaoCodeSystem<CodeSystem, Coding, CodeableConcept> {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(FhirResourceDaoCodeSystemDstu3.class);

	@Autowired
	private ITerminologySvc myTerminologySvc;

	@Autowired
	private ValidationSupportChain myValidationSupport;

	@Override
	public List<IIdType> findCodeSystemIdsContainingSystemAndCode(String theCode, String theSystem) {
		List<IIdType> valueSetIds;
		Set<Long> ids = searchForIds(CodeSystem.SP_CODE, new TokenParam(theSystem, theCode));
		valueSetIds = new ArrayList<IIdType>();
		for (Long next : ids) {
			valueSetIds.add(new IdType("CodeSystem", next));
		}
		return valueSetIds;
	}

	private LookupCodeResult lookup(List<ValueSetExpansionContainsComponent> theContains, String theSystem, String theCode) {
		for (ValueSetExpansionContainsComponent nextCode : theContains) {

			String system = nextCode.getSystem();
			String code = nextCode.getCode();
			if (theSystem.equals(system) && theCode.equals(code)) {
				LookupCodeResult retVal = new LookupCodeResult();
				retVal.setSearchedForCode(code);
				retVal.setSearchedForSystem(system);
				retVal.setFound(true);
				if (nextCode.getAbstractElement().getValue() != null) {
					retVal.setCodeIsAbstract(nextCode.getAbstractElement().booleanValue());
				}
				retVal.setCodeDisplay(nextCode.getDisplay());
				retVal.setCodeSystemVersion(nextCode.getVersion());
				retVal.setCodeSystemDisplayName("Unknown"); // TODO: implement
				return retVal;
			}

		}

		return null;
	}

	@Override
	public LookupCodeResult lookupCode(IPrimitiveType<String> theCode, IPrimitiveType<String> theSystem, Coding theCoding, RequestDetails theRequestDetails) {
		boolean haveCoding = theCoding != null && isNotBlank(theCoding.getSystem()) && isNotBlank(theCoding.getCode());
		boolean haveCode = theCode != null && theCode.isEmpty() == false;
		boolean haveSystem = theSystem != null && theSystem.isEmpty() == false;

		if (!haveCoding && !(haveSystem && haveCode)) {
			throw new InvalidRequestException("No code, coding, or codeableConcept provided to validate");
		}
		if (!LogicUtil.multiXor(haveCoding, (haveSystem && haveCode)) || (haveSystem != haveCode)) {
			throw new InvalidRequestException("$lookup can only validate (system AND code) OR (coding.system AND coding.code)");
		}

		String code;
		String system;
		if (haveCoding) {
			code = theCoding.getCode();
			system = theCoding.getSystem();
		} else {
			code = theCode.getValue();
			system = theSystem.getValue();
		}

		// CodeValidationResult validateOutcome = myJpaValidationSupport.validateCode(getContext(), system, code, null);
		//
		// LookupCodeResult result = new LookupCodeResult();
		// result.setSearchedForCode(code);
		// result.setSearchedForSystem(system);
		// result.setFound(false);
		// if (validateOutcome.isOk()) {
		// result.setFound(true);
		// result.setCodeIsAbstract(validateOutcome.asConceptDefinition().getAbstract());
		// result.setCodeDisplay(validateOutcome.asConceptDefinition().getDisplay());
		// }
		// return result;

		if (myValidationSupport.isCodeSystemSupported(getContext(), system)) {
			HapiWorkerContext ctx = new HapiWorkerContext(getContext(), myValidationSupport);
			ValueSetExpander expander = ctx.getExpander();
			ValueSet source = new ValueSet();
			source.getCompose().addInclude().setSystem(system).addConcept().setCode(code);

			ValueSetExpansionOutcome expansion;
			try {
				expansion = expander.expand(source);
			} catch (Exception e) {
				throw new InternalErrorException(e);
			}

			if (expansion.getValueset() != null) {
				List<ValueSetExpansionContainsComponent> contains = expansion.getValueset().getExpansion().getContains();
				LookupCodeResult result = lookup(contains, system, code);
				if (result != null) {
					return result;
				}
			}

		} else {

			/*
			 * If it's not a built-in code system, use ones from the database
			 */

			List<IIdType> valueSetIds = findCodeSystemIdsContainingSystemAndCode(code, system);
			for (IIdType nextId : valueSetIds) {
				CodeSystem expansion = read(nextId, theRequestDetails);
				for (ConceptDefinitionComponent next : expansion.getConcept()) {
					if (code.equals(next.getCode())) {
						LookupCodeResult retVal = new LookupCodeResult();
						retVal.setSearchedForCode(code);
						retVal.setSearchedForSystem(system);
						retVal.setFound(true);
						retVal.setCodeDisplay(next.getDisplay());
						retVal.setCodeSystemDisplayName("Unknown"); // TODO: implement
						return retVal;
					}
				}
			}

		}

		// We didn't find it..
		LookupCodeResult retVal = new LookupCodeResult();
		retVal.setFound(false);
		retVal.setSearchedForCode(code);
		retVal.setSearchedForSystem(system);
		return retVal;

	}

	private List<TermConcept> toPersistedConcepts(List<ConceptDefinitionComponent> theConcept, TermCodeSystemVersion theCodeSystemVersion) {
		ArrayList<TermConcept> retVal = new ArrayList<TermConcept>();

		for (ConceptDefinitionComponent next : theConcept) {
			if (isNotBlank(next.getCode())) {
				TermConcept termConcept = new TermConcept();
				termConcept.setCode(next.getCode());
				termConcept.setCodeSystem(theCodeSystemVersion);
				termConcept.setDisplay(next.getDisplay());
				termConcept.addChildren(toPersistedConcepts(next.getConcept(), theCodeSystemVersion));
				retVal.add(termConcept);
			}
		}

		return retVal;
	}

	@Override
	protected ResourceTable updateEntity(IBaseResource theResource, ResourceTable theEntity, boolean theUpdateHistory, Date theDeletedTimestampOrNull, boolean thePerformIndexing,
			boolean theUpdateVersion, Date theUpdateTime, RequestDetails theRequestDetails) {
		ResourceTable retVal = super.updateEntity(theResource, theEntity, theUpdateHistory, theDeletedTimestampOrNull, thePerformIndexing, theUpdateVersion, theUpdateTime, theRequestDetails);

		CodeSystem cs = (CodeSystem) theResource;
		String codeSystemUrl = cs.getUrl();

		if (isNotBlank(codeSystemUrl)) {
			if (cs.getContent() == CodeSystemContentMode.COMPLETE) {
				ourLog.info("CodeSystem {} has a status of {}, going to store concepts in terminology tables", retVal.getIdDt().getValue(), cs.getContent().toCode());
				TermCodeSystemVersion persCs = new TermCodeSystemVersion();
				persCs.setResource(retVal);
				persCs.setResourceVersionId(retVal.getVersion());
				persCs.getConcepts().addAll(toPersistedConcepts(cs.getConcept(), persCs));

				myTerminologySvc.storeNewCodeSystemVersion(retVal.getId(), codeSystemUrl, persCs);
			}
		}

		return retVal;
	}

}
