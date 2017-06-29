package fi.fmi.avi.converter.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fi.fmi.avi.converter.ConversionIssue;
import fi.fmi.avi.converter.ConversionResult;
import fi.fmi.avi.data.AviationWeatherMessage;

/**
 *
 * @author Ilkka Rinne / Spatineo Oy 2017
 */
public class ConversionResultImpl<T> implements ConversionResult<T> {

    private T convertedMessage;
    private List<ConversionIssue> issues;

    public ConversionResultImpl() {
        issues = new ArrayList<>();
    }

    @Override
    public Status getStatus() {
        if (convertedMessage == null) {
            return Status.FAIL;
        } else if (this.issues.size() == 0) {
            return Status.SUCCESS;
        } else {
            return Status.WITH_ERRORS;
        }
    }

    @Override
    public T getConvertedMessage() {
        return this.convertedMessage;
    }

    @Override
    public List<ConversionIssue> getConversionIssues() {
        return this.issues;
    }


    public void setConvertedMessage(T message) {
        this.convertedMessage = message;
    }

    public void addIssue(ConversionIssue issue) {
        if (issue != null) {
            this.issues.add(issue);
        }
    }

    public void addIssue(Collection<ConversionIssue> issues) {
        if (issues != null && !issues.isEmpty()) {
            this.issues.addAll(issues);
        }
    }

}
