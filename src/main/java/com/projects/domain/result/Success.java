package com.projects.domain.result;

import java.util.Optional;


record Success<T>(T value) implements Result<T> {


    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public Optional<T> getValue() {
        return Optional.ofNullable(value);
    }

    @Override
    public Optional<DomainError> getError() {
        return Optional.empty();
    }
}