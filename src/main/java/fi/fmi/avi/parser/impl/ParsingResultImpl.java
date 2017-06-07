package fi.fmi.avi.parser.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fi.fmi.avi.data.AviationWeatherMessage;
import fi.fmi.avi.parser.ParsingIssue;
import fi.fmi.avi.parser.ParsingResult;

/**
 *
 * @author Ilkka Rinne / Spatineo Oy 2017
 */
public class ParsingResultImpl<T extends AviationWeatherMessage> implements ParsingResult<T> {

    private T parsedMessage;
    private List<ParsingIssue> issues;

    public ParsingResultImpl() {
        issues = new ArrayList<>();
    }

    @Override
    public ParsingStatus getStatus() {
        if (parsedMessage == null) {
            return ParsingStatus.FAIL;
        } else if (this.issues.size() == 0) {
            return ParsingStatus.SUCCESS;
        } else {
            return ParsingStatus.WITH_ERRORS;
        }
    }

    @Override
    public T getParsedMessage() {
        return this.parsedMessage;
    }

    @Override
    public List<ParsingIssue> getParsingIssues() {
        return this.issues;
    }


    public void setParsedMessage(T message) {
        this.parsedMessage = message;
    }

    public void addIssue(ParsingIssue issue) {
        if (issue != null) {
            this.issues.add(issue);
        }
    }

    public void addIssue(Collection<ParsingIssue> issues) {
        if (issues != null && !issues.isEmpty()) {
            this.issues.addAll(issues);
        }
    }

}
