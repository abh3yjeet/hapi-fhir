package ca.uhn.fhir.jpa.term;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Stopwatch;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.dao.data.ITermCodeSystemDao;
import ca.uhn.fhir.jpa.dao.data.ITermCodeSystemVersionDao;
import ca.uhn.fhir.jpa.dao.data.ITermConceptDao;
import ca.uhn.fhir.jpa.dao.data.ITermConceptParentChildLinkDao;
import ca.uhn.fhir.jpa.entity.TermCodeSystem;
import ca.uhn.fhir.jpa.entity.TermCodeSystemVersion;
import ca.uhn.fhir.jpa.entity.TermConcept;
import ca.uhn.fhir.jpa.entity.TermConceptParentChildLink;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ca.uhn.fhir.util.ObjectUtil;
import ca.uhn.fhir.util.ValidateUtil;

public class TerminologySvcImpl implements ITerminologySvc {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(TerminologySvcImpl.class);
	private static final Object PLACEHOLDER_OBJECT = new Object();

	@Autowired
	private ITermCodeSystemDao myCodeSystemDao;

	@Autowired
	private ITermCodeSystemVersionDao myCodeSystemVersionDao;

	@Autowired
	private ITermConceptDao myConceptDao;

	@Autowired
	private ITermConceptParentChildLinkDao myConceptParentChildLinkDao;

	@Autowired
	private FhirContext myContext;

	private void fetchChildren(TermConcept theConcept, Set<TermConcept> theSetToPopulate) {
		for (TermConceptParentChildLink nextChildLink : theConcept.getChildren()) {
			TermConcept nextChild = nextChildLink.getChild();
			if (theSetToPopulate.add(nextChild)) {
				fetchChildren(nextChild, theSetToPopulate);
			}
		}
	}

	private TermConcept fetchLoadedCode(Long theCodeSystemResourcePid, Long theCodeSystemVersionPid, String theCode) {
		TermCodeSystemVersion codeSystem = myCodeSystemVersionDao.findByCodeSystemResourceAndVersion(theCodeSystemResourcePid, theCodeSystemVersionPid);
		TermConcept concept = myConceptDao.findByCodeSystemAndCode(codeSystem, theCode);
		return concept;
	}

	private void fetchParents(TermConcept theConcept, Set<TermConcept> theSetToPopulate) {
		for (TermConceptParentChildLink nextChildLink : theConcept.getParents()) {
			TermConcept nextChild = nextChildLink.getParent();
			if (theSetToPopulate.add(nextChild)) {
				fetchParents(nextChild, theSetToPopulate);
			}
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public Set<TermConcept> findCodesAbove(Long theCodeSystemResourcePid, Long theCodeSystemVersionPid, String theCode) {
		Stopwatch stopwatch = Stopwatch.createStarted();

		TermConcept concept = fetchLoadedCode(theCodeSystemResourcePid, theCodeSystemVersionPid, theCode);
		if (concept == null) {
			return Collections.emptySet();
		}

		Set<TermConcept> retVal = new HashSet<TermConcept>();
		retVal.add(concept);

		fetchParents(concept, retVal);

		ourLog.info("Fetched {} codes above code {} in {}ms", new Object[] { retVal.size(), theCode, stopwatch.elapsed(TimeUnit.MILLISECONDS) });
		return retVal;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public Set<TermConcept> findCodesBelow(Long theCodeSystemResourcePid, Long theCodeSystemVersionPid, String theCode) {
		Stopwatch stopwatch = Stopwatch.createStarted();

		TermConcept concept = fetchLoadedCode(theCodeSystemResourcePid, theCodeSystemVersionPid, theCode);
		if (concept == null) {
			return Collections.emptySet();
		}

		Set<TermConcept> retVal = new HashSet<TermConcept>();
		retVal.add(concept);

		fetchChildren(concept, retVal);

		ourLog.info("Fetched {} codes below code {} in {}ms", new Object[] { retVal.size(), theCode, stopwatch.elapsed(TimeUnit.MILLISECONDS) });
		return retVal;
	}

	private void persistChildren(TermConcept theConcept, TermCodeSystemVersion theCodeSystem, IdentityHashMap<TermConcept, Object> theConceptsStack) {
		if (theConceptsStack.put(theConcept, PLACEHOLDER_OBJECT) != null) {
			return;
		}

		for (TermConceptParentChildLink next : theConcept.getChildren()) {
			persistChildren(next.getChild(), theCodeSystem, theConceptsStack);
		}

		myConceptDao.save(theConcept);

		for (TermConceptParentChildLink next : theConcept.getChildren()) {
			myConceptParentChildLinkDao.save(next);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void storeNewCodeSystemVersion(Long theCodeSystemResourcePid, String theSystemUri, TermCodeSystemVersion theCodeSystem) {
		ourLog.info("Storing code system");

		ValidateUtil.isNotNullOrThrowInvalidRequest(theCodeSystem.getResource() != null, "No resource supplied");
		ValidateUtil.isNotBlankOrThrowInvalidRequest(theSystemUri, "No system URI supplied");

		TermCodeSystem codeSystem = myCodeSystemDao.findByCodeSystemUri(theSystemUri);
		if (codeSystem == null) {
			codeSystem = myCodeSystemDao.findByResourcePid(theCodeSystemResourcePid);
			if (codeSystem == null) {
				codeSystem = new TermCodeSystem();
			}
			codeSystem.setResource(theCodeSystem.getResource());
			codeSystem.setCodeSystemUri(theSystemUri);
			myCodeSystemDao.save(codeSystem);
		} else {
			if (!ObjectUtil.equals(codeSystem.getResource().getId(), theCodeSystem.getResource().getId())) {
				String msg = myContext.getLocalizer().getMessage(TerminologySvcImpl.class, "cannotCreateDuplicateCodeSystemUri", theSystemUri, codeSystem.getResource().getIdDt().toUnqualifiedVersionless().getValue());
				throw new UnprocessableEntityException(msg);
			}
		}

		// Validate the code system
		IdentityHashMap<TermConcept, Object> conceptsStack = new IdentityHashMap<TermConcept, Object>();
		for (TermConcept next : theCodeSystem.getConcepts()) {
			validateConceptForStorage(next, theCodeSystem, conceptsStack);
		}

		myCodeSystemVersionDao.save(theCodeSystem);
		
		codeSystem.setCurrentVersion(theCodeSystem);
		myCodeSystemDao.save(codeSystem);

		conceptsStack = new IdentityHashMap<TermConcept, Object>();
		for (TermConcept next : theCodeSystem.getConcepts()) {
			persistChildren(next, theCodeSystem, conceptsStack);
		}
	}

	@Override
	public boolean supportsSystem(String theSystem) {
		TermCodeSystem cs = myCodeSystemDao.findByCodeSystemUri(theSystem);
		return cs != null;
	}

	private void validateConceptForStorage(TermConcept theConcept, TermCodeSystemVersion theCodeSystem, IdentityHashMap<TermConcept, Object> theConceptsStack) {
		ValidateUtil.isNotNullOrThrowInvalidRequest(theConcept.getCodeSystem() == theCodeSystem, "Codesystem contains a code which does not reference the codesystem");
		ValidateUtil.isNotBlankOrThrowInvalidRequest(theConcept.getCode(), "Codesystem contains a code which does not reference the codesystem");

		if (theConceptsStack.put(theConcept, PLACEHOLDER_OBJECT) != null) {
			throw new InvalidRequestException("CodeSystem contains circular reference around code " + theConcept.getCode());
		}

		for (TermConceptParentChildLink next : theConcept.getChildren()) {
			validateConceptForStorage(next.getChild(), theCodeSystem, theConceptsStack);
		}

		theConceptsStack.remove(theConcept);
	}

	@Override
	public Set<TermConcept> findCodesBelow(String theSystem, String theCode) {
		TermCodeSystem cs = myCodeSystemDao.findByCodeSystemUri(theSystem);
		TermCodeSystemVersion csv = cs.getCurrentVersion();
		
		return findCodesBelow(cs.getResource().getId(), csv.getResourceVersionId(), theCode);
	}

}
