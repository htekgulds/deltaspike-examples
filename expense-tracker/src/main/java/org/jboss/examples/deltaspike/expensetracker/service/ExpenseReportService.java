package org.jboss.examples.deltaspike.expensetracker.service;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.jboss.examples.deltaspike.expensetracker.app.exception.ApplicationException;
import org.jboss.examples.deltaspike.expensetracker.app.message.AppMessages;
import org.jboss.examples.deltaspike.expensetracker.data.ExpenseReportRepository;
import org.jboss.examples.deltaspike.expensetracker.model.Employee;
import static org.jboss.examples.deltaspike.expensetracker.model.EmployeeRole.*;
import org.jboss.examples.deltaspike.expensetracker.model.ExpenseReport;
import org.jboss.examples.deltaspike.expensetracker.model.ReportStatus;
import org.picketlink.authorization.annotations.LoggedIn;
import org.picketlink.authorization.annotations.RolesAllowed;

@Transactional
@LoggedIn
@RequestScoped
public class ExpenseReportService {

    @Inject
    private ExpenseReportRepository repo;

    @Inject
    private AppMessages msg;

    @RolesAllowed(ACCOUNTANT)
    public void assign(ExpenseReport report, Employee assignee) throws ApplicationException {
        if (report == null || assignee == null) {
            throw new IllegalArgumentException();
        }

        if (report.getAssignee() != null) {
            throw new ApplicationException(msg.reportAlreadyAssigned(report.getName()));
        }

        if (report.getReporter().equals(assignee)) {
            throw new ApplicationException(msg.cantAssignReportToSelf());
        }

        report.setAssignee(assignee);
        repo.save(report);
    }

    @RolesAllowed(EMPLOYEE)
    public void submit(ExpenseReport report) throws ApplicationException {
        if (report == null) {
            throw new IllegalArgumentException();
        }

        ReportStatus status = report.getStatus();
        if (status == ReportStatus.SUBMITTED || status == ReportStatus.APPROVED || status == ReportStatus.SETTLED) {
            throw new ApplicationException(msg.reportAlreadySubmitted(report.getName()));
        }

        report.setStatus(ReportStatus.SUBMITTED);
        repo.save(report);
    }

    @RolesAllowed(ACCOUNTANT)
    public void reject(ExpenseReport report) throws ApplicationException {
        if (report == null) {
            throw new IllegalArgumentException();
        }
        if (report.getAssignee() == null) {
            throw new ApplicationException(msg.reportNotAssigned(report.getName()));
        }

        report.setStatus(ReportStatus.REJECTED);
        repo.save(report);
    }

    @RolesAllowed(ACCOUNTANT)
    public void approve(ExpenseReport report) throws ApplicationException {
        if (report == null) {
            throw new IllegalArgumentException();
        }
        if (report.getAssignee() == null) {
            throw new ApplicationException(msg.reportNotAssigned(report.getName()));
        }

        report.setStatus(ReportStatus.APPROVED);
        repo.save(report);
    }

    @RolesAllowed(ACCOUNTANT)
    public void settle(ExpenseReport report) throws ApplicationException {
        if (report == null) {
            throw new IllegalArgumentException();
        }
        if (report.getStatus() != ReportStatus.APPROVED) {
            throw new ApplicationException(msg.reportNotApproved(report.getName()));
        }

        report.setStatus(ReportStatus.SETTLED);
        repo.save(report);
    }
}