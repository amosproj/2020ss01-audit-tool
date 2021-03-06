package com.amos2020.javabackend.integration_tests;

import com.amos2020.javabackend.JavaBackendApplication;
import com.amos2020.javabackend.entity.*;
import com.amos2020.javabackend.repository.AuditRepository;
import com.amos2020.javabackend.repository.ContactPersonRepository;
import com.amos2020.javabackend.repository.FacCritRepository;
import com.amos2020.javabackend.repository.QuestionRepository;
import com.amos2020.javabackend.rest_service.request.interview.CreateInterviewRequest;
import com.amos2020.javabackend.rest_service.request.interview.InterviewAddContactPersonRequest;
import com.amos2020.javabackend.rest_service.request.interview.InterviewPerson;
import com.amos2020.javabackend.rest_service.request.interview.UpdateInterviewRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = JavaBackendApplication.class
)
@AutoConfigureMockMvc
public class InterviewIntegrationTest {

    private static final String VALID_NAME = "TestAuditName";
    private static final String NULL_STRING = null;
    private static final String BLANK_STRING = "   ";
    private static final String STRING_257 = StringUtils.repeat("*", 257);
    private static final String STRING_256 = StringUtils.repeat("*", 256);
    private static final String STRING_1025 = StringUtils.repeat("*", 1025);
    private static final String STRING_1024 = StringUtils.repeat("*", 1024);
    private static final Date VALID_START_DATE = Date.valueOf("2020-06-22");
    private static final Date VALID_END_DATE = Date.valueOf("2020-06-22");
    private static final Date INVALID_END_DATE = Date.valueOf("2020-06-21");
    private static final Date NULL_DATE = null;
    private static final Date UPDATED_START_DATE = Date.valueOf("2000-01-01");
    private static final Date UPDATED_END_DATE = Date.valueOf("2000-02-02");
    private static final String VALID_NOTE = "testNote";
    private static final Salutation TEST_SALUTATION = Salutation.HERR;
    private static final String TEST_TITLE = "TestTitle";
    private static final String TEST_INFORMATION = "0123456789, valid@email.com";
    private static final String TEST_FORENAME = "Jon";
    private static final String TEST_SURNAME = "Doe";
    private static final String TEST_COMPANY = "testCompany";
    private static final String TEST_DEPARTMENT = "testDepartment";
    private static final String TEST_SECTOR = "testSector";
    private static final String TEST_CORPORATE_DIVISION = "testDivision";
    @Autowired
    private MockMvc mvc;
    @Autowired
    private AuditRepository auditRepository;
    @Autowired
    private ContactPersonRepository contactPersonRepository;
    @Autowired
    private FacCritRepository facCritRepository;
    @Autowired
    private QuestionRepository questionRepository;
    private Audit audit;
    private List<ContactPerson> contactPersons = new ArrayList<>();
    private List<Integer> facCritIds = new ArrayList<>();
    private List<InterviewPerson> interviewedPeople = new ArrayList<>();

    @Before
    public void setup() {
        audit = new Audit();
        audit.setName(VALID_NAME);
        audit.setStartDate(VALID_START_DATE);
        audit.setStartDate(VALID_END_DATE);
        audit.setCreationDate(Timestamp.from(Instant.now()));
        audit.setStatus(AuditStatus.ACTIVE);
        auditRepository.save(audit);

        ContactPerson contactPerson = new ContactPerson();
        contactPerson.setSalutation(TEST_SALUTATION);
        contactPerson.setTitle(TEST_TITLE);
        contactPerson.setContactInformation(TEST_INFORMATION);
        contactPerson.setForename(TEST_FORENAME);
        contactPerson.setSurname(TEST_SURNAME);
        contactPerson.setCompanyName(TEST_COMPANY);
        contactPerson.setDepartment(TEST_DEPARTMENT);
        contactPerson.setSector(TEST_SECTOR);
        contactPerson.setCorporateDivision(TEST_CORPORATE_DIVISION);
        contactPerson = contactPersonRepository.save(contactPerson);

        contactPersons.add(contactPerson);
        interviewedPeople.add(new InterviewPerson(contactPerson.getId(), "TestRole"));

        FacCrit facCrit = new FacCrit();
        facCrit.setName("Effektivit??t");
        facCrit = facCritRepository.save(facCrit);

        facCritIds.add(facCrit.getId());

        Question question = new Question();
        question.setTextDe("Frage 1");
        question.setFaccritId(facCrit.getId());
        questionRepository.save(question);

    }

    @Test
    public void getInterviewWithValidId_returnsOk() throws Exception {
        String interviewId = setupNewInterview();

        mvc.perform(get("/interviews/" + interviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auditId").value(audit.getId()))
                .andExpect(jsonPath("$.startDate").value(VALID_START_DATE.toString()))
                .andExpect(jsonPath("$.endDate").value(VALID_END_DATE.toString()))
                .andExpect(jsonPath("$.status").value(InterviewStatus.ACTIVE.toString()))
                .andExpect(jsonPath("$.note").value(VALID_NOTE))
                .andExpect(jsonPath("$.answers", hasSize(1)))
                .andExpect(jsonPath("$.interviewedContactPersons", hasSize(1)))
                .andExpect(jsonPath("$.interviewedContactPersons[0].id").value(contactPersons.get(0).getId()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].salutation").value(contactPersons.get(0).getSalutation().toString()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].title").value(contactPersons.get(0).getTitle()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].forename").value(contactPersons.get(0).getForename()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].surname").value(contactPersons.get(0).getSurname()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].contactInformation").value(contactPersons.get(0).getContactInformation()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].companyName").value(contactPersons.get(0).getCompanyName()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].department").value(contactPersons.get(0).getDepartment()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].sector").value(contactPersons.get(0).getSector()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].corporateDivision").value(contactPersons.get(0).getCorporateDivision()))
                .andReturn();
    }

    @Test
    public void getInterviewWithInvalidId_returnsNotFound() throws Exception {
        mvc.perform(get("/interviews/" + 100))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getAllInterviews() throws Exception {
        setupNewInterview();
        mvc.perform(get("/interviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThan(0)))
                .andReturn();
    }

    @Test
    public void createInterviewWithValidRequest_returnsOk() throws Exception {
        CreateInterviewRequest request = getCreateInterviewRequest(audit.getId(), VALID_START_DATE, VALID_END_DATE, VALID_NOTE, interviewedPeople, facCritIds);
        String requestAsJson = buildJson(request);

        mvc.perform(post("/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auditId").value(audit.getId()))
                .andExpect(jsonPath("$.startDate").value(VALID_START_DATE.toString()))
                .andExpect(jsonPath("$.endDate").value(VALID_END_DATE.toString()))
                .andExpect(jsonPath("$.status").value(InterviewStatus.ACTIVE.toString()))
                .andExpect(jsonPath("$.note").value(VALID_NOTE))
                .andExpect(jsonPath("$.answers", hasSize(1)))
                .andExpect(jsonPath("$.interviewedContactPersons", hasSize(1)))
                .andExpect(jsonPath("$.interviewedContactPersons[0].id").value(contactPersons.get(0).getId()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].salutation").value(contactPersons.get(0).getSalutation().toString()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].title").value(contactPersons.get(0).getTitle()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].forename").value(contactPersons.get(0).getForename()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].surname").value(contactPersons.get(0).getSurname()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].contactInformation").value(contactPersons.get(0).getContactInformation()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].companyName").value(contactPersons.get(0).getCompanyName()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].department").value(contactPersons.get(0).getDepartment()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].sector").value(contactPersons.get(0).getSector()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].corporateDivision").value(contactPersons.get(0).getCorporateDivision()))
                .andReturn();
    }


    @Test
    public void createInterviewWithAuditIdIsInvalid_returns400() throws Exception {
        CreateInterviewRequest request = getCreateInterviewRequest(0, VALID_START_DATE, VALID_END_DATE, VALID_NOTE, interviewedPeople, facCritIds);
        String requestAsJson = buildJson(request);

        mvc.perform(post("/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createInterviewWithAuditDoesNotExist_returns404() throws Exception {
        CreateInterviewRequest request = getCreateInterviewRequest(10000, VALID_START_DATE, VALID_END_DATE, VALID_NOTE, interviewedPeople, facCritIds);
        String requestAsJson = buildJson(request);

        mvc.perform(post("/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createInterviewWithStartDateIsNull_returns400() throws Exception {
        CreateInterviewRequest request = getCreateInterviewRequest(audit.getId(), NULL_DATE, VALID_END_DATE, VALID_NOTE, interviewedPeople, facCritIds);
        String requestAsJson = buildJson(request);

        mvc.perform(post("/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createInterviewWithEndDateIsNull_returns200() throws Exception {
        CreateInterviewRequest request = getCreateInterviewRequest(audit.getId(), VALID_START_DATE, NULL_DATE, VALID_NOTE, interviewedPeople, facCritIds);
        String requestAsJson = buildJson(request);

        mvc.perform(post("/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isOk());
    }

    @Test
    public void createInterviewWithStartDateIsAfterEndDate_returns400() throws Exception {
        CreateInterviewRequest request = getCreateInterviewRequest(audit.getId(), VALID_START_DATE, INVALID_END_DATE, VALID_NOTE, interviewedPeople, facCritIds);
        String requestAsJson = buildJson(request);

        mvc.perform(post("/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createInterviewWithContactPersonIdIsInvalid_returns400() throws Exception {
        interviewedPeople.add(new InterviewPerson(0, "Invalid"));
        CreateInterviewRequest request = getCreateInterviewRequest(audit.getId(), VALID_START_DATE, VALID_END_DATE, VALID_NOTE, interviewedPeople, facCritIds);
        String requestAsJson = buildJson(request);

        mvc.perform(post("/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createInterviewWithContactPersonNotExisting_returns404() throws Exception {
        interviewedPeople.add(new InterviewPerson(100, "Not existing"));
        CreateInterviewRequest request = getCreateInterviewRequest(audit.getId(), VALID_START_DATE, VALID_END_DATE, VALID_NOTE, interviewedPeople, facCritIds);
        String requestAsJson = buildJson(request);

        mvc.perform(post("/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createInterviewWithFaccritNotValid_returns400() throws Exception {
        facCritIds.add(0);
        CreateInterviewRequest request = getCreateInterviewRequest(audit.getId(), VALID_START_DATE, VALID_END_DATE, VALID_NOTE, interviewedPeople, facCritIds);
        String requestAsJson = buildJson(request);

        mvc.perform(post("/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createInterviewWithFaccritNotExisting_returns404() throws Exception {
        facCritIds.add(10000);
        CreateInterviewRequest request = getCreateInterviewRequest(audit.getId(), VALID_START_DATE, VALID_END_DATE, VALID_NOTE, interviewedPeople, facCritIds);
        String requestAsJson = buildJson(request);

        mvc.perform(post("/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createInterviewWithNoteIsNull_returns400() throws Exception {
        CreateInterviewRequest request = getCreateInterviewRequest(audit.getId(), VALID_START_DATE, VALID_END_DATE, NULL_STRING, interviewedPeople, facCritIds);
        String requestAsJson = buildJson(request);

        mvc.perform(post("/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createInterviewWithNoteIsTooLong_returns400() throws Exception {
        CreateInterviewRequest request = getCreateInterviewRequest(audit.getId(), VALID_START_DATE, VALID_END_DATE, STRING_1025, interviewedPeople, facCritIds);
        String requestAsJson = buildJson(request);

        mvc.perform(post("/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createInterviewWithNoteIsMaximum_returns200() throws Exception {
        CreateInterviewRequest request = getCreateInterviewRequest(audit.getId(), VALID_START_DATE, VALID_END_DATE, STRING_1024, interviewedPeople, facCritIds);
        String requestAsJson = buildJson(request);

        mvc.perform(post("/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isOk());
    }

    @Test
    public void updateInterviewWithValidRequest_returns200() throws Exception {
        String interviewId = setupNewInterview();
        UpdateInterviewRequest request = getUpdateInterviewRequest(UPDATED_START_DATE, UPDATED_END_DATE, InterviewStatus.FINISHED, VALID_NOTE);
        String requestAsJson = buildJson(request);
        mvc.perform(put("/interviews/" + interviewId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auditId").value(audit.getId()))
                .andExpect(jsonPath("$.startDate").value(UPDATED_START_DATE.toString()))
                .andExpect(jsonPath("$.endDate").value(UPDATED_END_DATE.toString()))
                .andExpect(jsonPath("$.note").value(VALID_NOTE))
                .andExpect(jsonPath("$.status").value(InterviewStatus.FINISHED.toString()))
                .andExpect(jsonPath("$.answers", hasSize(1)))
                .andExpect(jsonPath("$.interviewedContactPersons", hasSize(1)))
                .andExpect(jsonPath("$.interviewedContactPersons[0].id").value(contactPersons.get(0).getId()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].salutation").value(contactPersons.get(0).getSalutation().toString()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].title").value(contactPersons.get(0).getTitle()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].forename").value(contactPersons.get(0).getForename()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].surname").value(contactPersons.get(0).getSurname()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].contactInformation").value(contactPersons.get(0).getContactInformation()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].companyName").value(contactPersons.get(0).getCompanyName()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].department").value(contactPersons.get(0).getDepartment()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].sector").value(contactPersons.get(0).getSector()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].corporateDivision").value(contactPersons.get(0).getCorporateDivision()))
                .andReturn();
    }

    @Test
    public void updateInterviewWithStartDateIsNull_returns400() throws Exception {
        String interviewId = setupNewInterview();
        UpdateInterviewRequest request = getUpdateInterviewRequest(NULL_DATE, UPDATED_END_DATE, InterviewStatus.FINISHED, VALID_NOTE);
        String requestAsJson = buildJson(request);
        mvc.perform(put("/interviews/" + interviewId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateInterviewWithEndDateIsNull_returns200() throws Exception {
        String interviewId = setupNewInterview();
        UpdateInterviewRequest request = getUpdateInterviewRequest(UPDATED_START_DATE, NULL_DATE, InterviewStatus.FINISHED, VALID_NOTE);
        String requestAsJson = buildJson(request);
        mvc.perform(put("/interviews/" + interviewId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auditId").value(audit.getId()))
                .andExpect(jsonPath("$.startDate").value(UPDATED_START_DATE.toString()))
                .andExpect(jsonPath("$.endDate").value(nullValue()))
                .andExpect(jsonPath("$.status").value(InterviewStatus.FINISHED.toString()))
                .andExpect(jsonPath("$.note").value(VALID_NOTE))
                .andExpect(jsonPath("$.answers", hasSize(1)))
                .andExpect(jsonPath("$.interviewedContactPersons", hasSize(1)))
                .andExpect(jsonPath("$.interviewedContactPersons[0].id").value(contactPersons.get(0).getId()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].salutation").value(contactPersons.get(0).getSalutation().toString()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].title").value(contactPersons.get(0).getTitle()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].forename").value(contactPersons.get(0).getForename()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].surname").value(contactPersons.get(0).getSurname()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].contactInformation").value(contactPersons.get(0).getContactInformation()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].companyName").value(contactPersons.get(0).getCompanyName()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].department").value(contactPersons.get(0).getDepartment()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].sector").value(contactPersons.get(0).getSector()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].corporateDivision").value(contactPersons.get(0).getCorporateDivision()))
                .andReturn();
    }

    @Test
    public void updateInterviewWithEndDateIsBeforeStartDate_returns400() throws Exception {
        String interviewId = setupNewInterview();
        UpdateInterviewRequest request = getUpdateInterviewRequest(VALID_START_DATE, INVALID_END_DATE, InterviewStatus.FINISHED, VALID_NOTE);
        String requestAsJson = buildJson(request);
        mvc.perform(put("/interviews/" + interviewId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateInterviewWithInterviewStatusIsNull_returns400() throws Exception {
        String interviewId = setupNewInterview();
        UpdateInterviewRequest request = getUpdateInterviewRequest(UPDATED_START_DATE, UPDATED_END_DATE, null, VALID_NOTE);
        String requestAsJson = buildJson(request);
        mvc.perform(put("/interviews/" + interviewId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateInterviewWithNoInterviewExistingForInterviewId_returns404() throws Exception {
        UpdateInterviewRequest request = getUpdateInterviewRequest(UPDATED_START_DATE, UPDATED_END_DATE, InterviewStatus.ACTIVE, VALID_NOTE);
        String requestAsJson = buildJson(request);
        mvc.perform(put("/interviews/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateInterviewWithNoteIsNull_returns400() throws Exception {
        String interviewId = setupNewInterview();
        UpdateInterviewRequest request = getUpdateInterviewRequest(UPDATED_START_DATE, UPDATED_END_DATE, InterviewStatus.ACTIVE, NULL_STRING);
        String requestAsJson = buildJson(request);
        mvc.perform(put("/interviews/" + interviewId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateInterviewWithNoteIsTooLong_returns400() throws Exception {
        String interviewId = setupNewInterview();
        UpdateInterviewRequest request = getUpdateInterviewRequest(UPDATED_START_DATE, UPDATED_END_DATE, InterviewStatus.ACTIVE, STRING_1025);
        String requestAsJson = buildJson(request);
        mvc.perform(put("/interviews/" + interviewId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateInterviewWithNoteIsMaximum_returns200() throws Exception {
        String interviewId = setupNewInterview();
        UpdateInterviewRequest request = getUpdateInterviewRequest(UPDATED_START_DATE, UPDATED_END_DATE, InterviewStatus.ACTIVE, STRING_1024);
        String requestAsJson = buildJson(request);
        mvc.perform(put("/interviews/" + interviewId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isOk());
    }

    @Test
    public void addContactPersonToInterviewWithValidRequest_returns200() throws Exception {
        String interviewId = setupNewInterview();
        int contactPersonId = setupNewContactPerson();
        InterviewAddContactPersonRequest request = getInterviewAddContactPersonRequest(contactPersonId, "neue ContactPerson");
        String requestAsJson = buildJson(request);

        mvc.perform(put("/interviews/" + interviewId + "/add/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auditId").value(audit.getId()))
                .andExpect(jsonPath("$.startDate").value(VALID_START_DATE.toString()))
                .andExpect(jsonPath("$.endDate").value(VALID_END_DATE.toString()))
                .andExpect(jsonPath("$.status").value(InterviewStatus.ACTIVE.toString()))
                .andExpect(jsonPath("$.note").value(VALID_NOTE))
                .andExpect(jsonPath("$.answers", hasSize(1)))
                .andExpect(jsonPath("$.interviewedContactPersons", hasSize(2)))
                .andExpect(jsonPath("$.interviewedContactPersons[0].id").value(contactPersons.get(0).getId()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].salutation").value(contactPersons.get(0).getSalutation().toString()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].title").value(contactPersons.get(0).getTitle()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].forename").value(contactPersons.get(0).getForename()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].surname").value(contactPersons.get(0).getSurname()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].contactInformation").value(contactPersons.get(0).getContactInformation()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].companyName").value(contactPersons.get(0).getCompanyName()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].department").value(contactPersons.get(0).getDepartment()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].sector").value(contactPersons.get(0).getSector()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].corporateDivision").value(contactPersons.get(0).getCorporateDivision()))
                .andExpect(jsonPath("$.interviewedContactPersons[1].id").value(contactPersonId))
                .andExpect(jsonPath("$.interviewedContactPersons[1].salutation").value(TEST_SALUTATION.toString()))
                .andExpect(jsonPath("$.interviewedContactPersons[1].title").value(TEST_TITLE))
                .andExpect(jsonPath("$.interviewedContactPersons[1].forename").value(TEST_FORENAME))
                .andExpect(jsonPath("$.interviewedContactPersons[1].surname").value(TEST_SURNAME))
                .andExpect(jsonPath("$.interviewedContactPersons[1].contactInformation").value(TEST_INFORMATION))
                .andExpect(jsonPath("$.interviewedContactPersons[1].companyName").value(TEST_COMPANY))
                .andExpect(jsonPath("$.interviewedContactPersons[1].department").value(TEST_DEPARTMENT))
                .andExpect(jsonPath("$.interviewedContactPersons[1].sector").value(TEST_SECTOR))
                .andExpect(jsonPath("$.interviewedContactPersons[1].corporateDivision").value(TEST_CORPORATE_DIVISION))
                .andReturn();
    }

    @Test
    public void addContactPersonToInterviewWithInvalidInterviewId_returns400() throws Exception {
        int contactPersonId = setupNewContactPerson();
        InterviewAddContactPersonRequest request = getInterviewAddContactPersonRequest(contactPersonId, "neue ContactPerson");
        String requestAsJson = buildJson(request);

        mvc.perform(put("/interviews/0/add/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addContactPersonToInterviewWithInterviewIdIsNotExisting_returns404() throws Exception {
        int contactPersonId = setupNewContactPerson();
        InterviewAddContactPersonRequest request = getInterviewAddContactPersonRequest(contactPersonId, "neue ContactPerson");
        String requestAsJson = buildJson(request);

        mvc.perform(put("/interviews/100/add/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isNotFound());
    }

    @Test
    public void addContactPersonToInterviewWithInvalidContactPersonId_returns400() throws Exception {
        String interviewId = setupNewInterview();
        InterviewAddContactPersonRequest request = getInterviewAddContactPersonRequest(0, "neue ContactPerson");
        String requestAsJson = buildJson(request);

        mvc.perform(put("/interviews/" + interviewId + "/add/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addContactPersonToInterviewWithContactPersonIdIsNotExisting_returns404() throws Exception {
        String interviewId = setupNewInterview();
        InterviewAddContactPersonRequest request = getInterviewAddContactPersonRequest(100, "neue ContactPerson");
        String requestAsJson = buildJson(request);

        mvc.perform(put("/interviews/" + interviewId + "/add/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isNotFound());
    }

    @Test
    public void addContactPersonToInterviewWithRoleIsNull_returns400() throws Exception {
        String interviewId = setupNewInterview();
        int contactPersonId = setupNewContactPerson();
        InterviewAddContactPersonRequest request = getInterviewAddContactPersonRequest(contactPersonId, NULL_STRING);
        String requestAsJson = buildJson(request);

        mvc.perform(put("/interviews/" + interviewId + "/add/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addContactPersonToInterviewWithRoleIsBlank_returns400() throws Exception {
        String interviewId = setupNewInterview();
        int contactPersonId = setupNewContactPerson();
        InterviewAddContactPersonRequest request = getInterviewAddContactPersonRequest(contactPersonId, BLANK_STRING);
        String requestAsJson = buildJson(request);

        mvc.perform(put("/interviews/" + interviewId + "/add/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addContactPersonToInterviewWithRoleIsToLong_returns400() throws Exception {
        String interviewId = setupNewInterview();
        int contactPersonId = setupNewContactPerson();
        InterviewAddContactPersonRequest request = getInterviewAddContactPersonRequest(contactPersonId, STRING_257);
        String requestAsJson = buildJson(request);

        mvc.perform(put("/interviews/" + interviewId + "/add/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addContactPersonToInterviewWithRoleIsToMaximum_returns200() throws Exception {
        String interviewId = setupNewInterview();
        int contactPersonId = setupNewContactPerson();
        InterviewAddContactPersonRequest request = getInterviewAddContactPersonRequest(contactPersonId, STRING_256);
        String requestAsJson = buildJson(request);

        mvc.perform(put("/interviews/" + interviewId + "/add/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isOk());
    }

    @Test
    public void removeContactPersonFromInterviewWithValidParameters_returns200() throws Exception {
        String interviewId = setupNewInterview();
        mvc.perform(delete("/interviews/" + interviewId + "/delete/person/" + contactPersons.get(0).getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auditId").value(audit.getId()))
                .andExpect(jsonPath("$.startDate").value(VALID_START_DATE.toString()))
                .andExpect(jsonPath("$.endDate").value(VALID_END_DATE.toString()))
                .andExpect(jsonPath("$.status").value(InterviewStatus.ACTIVE.toString()))
                .andExpect(jsonPath("$.answers", hasSize(1)))
                .andExpect(jsonPath("$.interviewedContactPersons", hasSize(0)))
                .andReturn();
    }

    @Test
    public void removeContactPersonFromInterviewWithInterviewIdNotExisting_returns404() throws Exception {
        mvc.perform(delete("/interviews/100/delete/person/" + contactPersons.get(0).getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void removeContactPersonFromInterviewWithInvalidInterviewId_returns400() throws Exception {
        mvc.perform(delete("/interviews/0/delete/person/" + contactPersons.get(0).getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void removeContactPersonFromInterviewWithContactPersonIdNotExisting_returns404() throws Exception {
        String interviewId = setupNewInterview();
        mvc.perform(delete("/interviews/" + interviewId + "/delete/person/100"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void removeContactPersonFromInterviewWithInvalidContactPersonId_returns400() throws Exception {
        String interviewId = setupNewInterview();
        mvc.perform(delete("/interviews/" + interviewId + "/delete/person/0"))
                .andExpect(status().isBadRequest());
    }

    private String setupNewInterview() throws Exception {
        CreateInterviewRequest request = getCreateInterviewRequest(audit.getId(), VALID_START_DATE, VALID_END_DATE, VALID_NOTE, interviewedPeople, facCritIds);
        String requestAsJson = buildJson(request);

        String result = mvc.perform(post("/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestAsJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auditId").value(audit.getId()))
                .andExpect(jsonPath("$.startDate").value(VALID_START_DATE.toString()))
                .andExpect(jsonPath("$.endDate").value(VALID_END_DATE.toString()))
                .andExpect(jsonPath("$.status").value(InterviewStatus.ACTIVE.toString()))
                .andExpect(jsonPath("$.note").value(VALID_NOTE))
                .andExpect(jsonPath("$.answers", hasSize(1)))
                .andExpect(jsonPath("$.interviewedContactPersons", hasSize(1)))
                .andExpect(jsonPath("$.interviewedContactPersons[0].id").value(contactPersons.get(0).getId()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].salutation").value(contactPersons.get(0).getSalutation().toString()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].title").value(contactPersons.get(0).getTitle()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].forename").value(contactPersons.get(0).getForename()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].surname").value(contactPersons.get(0).getSurname()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].contactInformation").value(contactPersons.get(0).getContactInformation()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].companyName").value(contactPersons.get(0).getCompanyName()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].department").value(contactPersons.get(0).getDepartment()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].sector").value(contactPersons.get(0).getSector()))
                .andExpect(jsonPath("$.interviewedContactPersons[0].corporateDivision").value(contactPersons.get(0).getCorporateDivision()))
                .andReturn().getResponse().getContentAsString().split(",")[0];

        return result.substring(result.indexOf(":") + 1);
    }

    private int setupNewContactPerson() {
        ContactPerson contactPerson = new ContactPerson();
        contactPerson.setSalutation(TEST_SALUTATION);
        contactPerson.setTitle(TEST_TITLE);
        contactPerson.setContactInformation(TEST_INFORMATION);
        contactPerson.setForename(TEST_FORENAME);
        contactPerson.setSurname(TEST_SURNAME);
        contactPerson.setCompanyName(TEST_COMPANY);
        contactPerson.setDepartment(TEST_DEPARTMENT);
        contactPerson.setSector(TEST_SECTOR);
        contactPerson.setCorporateDivision(TEST_CORPORATE_DIVISION);
        contactPerson = contactPersonRepository.save(contactPerson);
        return contactPerson.getId();
    }

    private CreateInterviewRequest getCreateInterviewRequest(int auditId, Date startDate, Date endDate, String note, List<InterviewPerson> interviewedPeople, List<Integer> scope) {
        CreateInterviewRequest request = new CreateInterviewRequest();
        request.setAuditId(auditId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setNote(note);
        request.setInterviewedContactPersons(interviewedPeople);
        request.setInterviewScope(scope);
        return request;
    }

    private UpdateInterviewRequest getUpdateInterviewRequest(Date startDate, Date endDate, InterviewStatus status, String note) {
        UpdateInterviewRequest request = new UpdateInterviewRequest();
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setStatus(status);
        request.setNote(note);
        return request;
    }

    private InterviewAddContactPersonRequest getInterviewAddContactPersonRequest(int contactPersonId, String role) {
        InterviewAddContactPersonRequest request = new InterviewAddContactPersonRequest();
        request.setContactPersonId(contactPersonId);
        request.setRole(role);
        return request;
    }


    private String buildJson(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        return mapper.writer().withDefaultPrettyPrinter().writeValueAsString(object);
    }

}
