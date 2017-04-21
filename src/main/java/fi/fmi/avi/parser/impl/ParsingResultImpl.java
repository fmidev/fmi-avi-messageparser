package fi.fmi.avi.parser.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.ParsingIssue;
import fi.fmi.avi.parser.ParsingResult;

/**
 * Created by rinne on 21/04/17.
 */
public class ParsingResultImpl<T extends AviationWeatherMessage> implements ParsingResult<T> {

    private ParsingStatus status;
    private T parsedMessage;
    private List<ParsingIssue> issues;

    ParsingResultImpl() {
        issues = new ArrayList<>();
        status = ParsingStatus.FAIL;
    }

    @Override
    public ParsingStatus getStatus() {
        return this.status;
    }

    @Override
    public T getParsedMessage() {
        return this.parsedMessage;
    }

    @Override
    public List<ParsingIssue> getParsingIssues() {
        return this.issues;
    }

    public void setStatus(final ParsingStatus status) {
        this.status = status;
    }

    public void setParsedMessage(T message) {
        this.parsedMessage = message;
        this.status = ParsingStatus.SUCCESS;
    }

    public void addIssue(ParsingIssue issue) {
        if (issue != null) {
            this.issues.add(issue);
            this.status = ParsingStatus.WITH_ERRORS;
        }
    }

    public void addIssue(Collection<ParsingIssue> issues) {
        if (issues != null && !issues.isEmpty()) {
            this.issues.addAll(issues);
            this.status = ParsingStatus.WITH_ERRORS;
        }
    }

}
