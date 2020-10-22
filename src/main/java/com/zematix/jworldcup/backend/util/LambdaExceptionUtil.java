package com.zematix.jworldcup.backend.util;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Hack to allow checked exceptions to be propagated through lambdas (from
 * Jedediah Smith)
 * 
 * @see <a href=
 *      "http://stackoverflow.com/questions/27644361/how-can-i-throw-checked-exceptions-from-inside-java-8-streams">
 *      more info </a>
 */
public final class LambdaExceptionUtil {

	@FunctionalInterface
	public interface Predicate_WithExceptions<T, E extends Exception> {
	    boolean test(T t) throws E;
	}

	@FunctionalInterface
	public interface Consumer_WithExceptions<T, E extends Exception> {
		void accept(T t) throws E;
	}

	@FunctionalInterface
	public interface BiConsumer_WithExceptions<T, U, E extends Exception> {
		void accept(T t, U u) throws E;
	}

	@FunctionalInterface
	public interface Function_WithExceptions<T, R, E extends Exception> {
		R apply(T t) throws E;
	}

	@FunctionalInterface
	public interface Supplier_WithExceptions<T, E extends Exception> {
		T get() throws E;
	}

	@FunctionalInterface
	public interface Runnable_WithExceptions<E extends Exception> {
		void run() throws E;
	}

	/**
	 * .filter(rethrowPredicate(t -> t.isActive()))
	 */
	public static <T, E extends Exception> Predicate<T> rethrowPredicate(Predicate_WithExceptions<T, E> predicate) throws E {
	    return t -> {
	        try {
	            return predicate.test(t);
	        } catch (Exception exception) {
	            return throwAsUnchecked(exception);
	        }
	    };
	}

	/**
	 * .forEach(rethrowConsumer(name -> System.out.println(Class.forName(name)))); or
	 * .forEach(rethrowConsumer(ClassNameUtil::println));
	 */
	public static <T, E extends Exception> Consumer<T> rethrowConsumer(Consumer_WithExceptions<T, E> consumer)
			throws E {
		return t -> {
			try {
				consumer.accept(t);
			} catch (Exception exception) {
				throwAsUnchecked(exception);
			}
		};
	}

	public static <T, U, E extends Exception> BiConsumer<T, U> rethrowBiConsumer(
			BiConsumer_WithExceptions<T, U, E> biConsumer) throws E {
		return (t, u) -> {
			try {
				biConsumer.accept(t, u);
			} catch (Exception exception) {
				throwAsUnchecked(exception);
			}
		};
	}

	/**
	 * .map(rethrowFunction(name -> Class.forName(name))) or
	 * .map(rethrowFunction(Class::forName))
	 */
	public static <T, R, E extends Exception> Function<T, R> rethrowFunction(Function_WithExceptions<T, R, E> function)
			throws E {
		return t -> {
			try {
				return function.apply(t);
			} catch (Exception exception) {
				return throwAsUnchecked(exception);
			}
		};
	}

	/**
	 * rethrowSupplier(() -> new StringJoiner(new String(new byte[]{77, 97, 114, 107}, "UTF-8"))),
	 */
	public static <T, E extends Exception> Supplier<T> rethrowSupplier(Supplier_WithExceptions<T, E> function)
			throws E {
		return () -> {
			try {
				return function.get();
			} catch (Exception exception) {
				return throwAsUnchecked(exception);
			}
		};
	}

	/** uncheck(() -> Class.forName("xxx")); */
	public static <E extends Exception> void uncheck(Runnable_WithExceptions<E> runnable) {
		try {
			runnable.run();
		} catch (Exception exception) {
			throwAsUnchecked(exception);
		}
	}

	/** uncheck(() -> Class.forName("xxx")); */
	public static <R, E extends Exception> R uncheck(Supplier_WithExceptions<R, E> supplier) {
		try {
			return supplier.get();
		} catch (Exception exception) {
			return throwAsUnchecked(exception);
		}
	}

	/** uncheck(Class::forName, "xxx"); */
	public static <T, R, E extends Exception> R uncheck(Function_WithExceptions<T, R, E> function, T t) {
		try {
			return function.apply(t);
		} catch (Exception exception) {
			return throwAsUnchecked(exception);
		}
	}

	/**
	 * Rethrows the given exception as unchecked. 
	 * 
	 * @param exception
	 * @return
	 * @throws E
	 * @see <a href="https://dzone.com/articles/sneakily-throwing-exceptions-in-lambda-expressions"> more info</a>
	 */
	@SuppressWarnings("unchecked")
	private static <T, E extends Exception> T throwAsUnchecked(Exception exception) throws E {
	    throw (E) exception;
	}
}