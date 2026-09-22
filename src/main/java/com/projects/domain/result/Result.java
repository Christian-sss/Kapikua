package com.projects.domain.result;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public sealed interface Result<T> permits Success, Failure {

    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }
    static Result<Void> success() {
        return new Success<>(null);
    }
    static <T> Result<T> failure(DomainError error) {
        DomainError seguro = (error != null) ? error : DomainError.of("Error no especificado.");
        return new Failure<>(seguro);
    }
    static <T> Result<T> failure(String message) {
        return failure(DomainError.of(message));
    }
    static <T> Result<T> failure(String code, String message) {
        return failure(DomainError.of(code, message));
    }

    // --- Consultas de estado ---
    boolean isSuccess();

    default boolean isFailure() {
        return !isSuccess();
    }

    Optional<T> getValue();
    Optional<DomainError> getError();

    // Transactions

    default <R> Result<R> map(Function<? super T, ? extends R> mapper) {
        if (this instanceof Success<T> s) {
            return Result.success(mapper.apply(s.value()));
        }
        if (this instanceof Failure<T> f) {
            return Result.failure(f.error());
        }
        return Result.failure("Estado desconocido");
    }




    default <R> Result<R> flatMap(Function<? super T, Result<R>> mapper) {
        if (this instanceof Success<T> s) {
            return mapper.apply(s.value());
        }
        if (this instanceof Failure<T> f) {
            return Result.failure(f.error());
        }
        return Result.failure("Estado desconocido");
    }



    default Result<T> onSuccess(Consumer<? super T> action) {
        if (this instanceof Success<T> s) {
            action.accept(s.value());
        }
        return this;
    }
    default Result<T> onFailure(Consumer<DomainError> action) {
        if (this instanceof Failure<T> f) {
            action.accept(f.error());
        }
        return this;
    }


}