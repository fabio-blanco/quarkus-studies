package br.com.globalbyte.samples.quarkus.hibernate.orm.reactive.panache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.CompositeException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.hibernate.HibernateException;
import org.hibernate.PersistentObjectException;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletionException;

@Path("/fruits")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FruitResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(FruitResource.class);


    @GET
    public Uni<List<Fruit>> get() {
        return Fruit.listAll(Sort.by("name"));
    }

    @GET
    @Path("/{id}")
    public Uni<Fruit> getSingle(Long id) {
        return Fruit.findById(id);
    }

    @POST
    public Uni<RestResponse<Fruit>> create(Fruit fruit) {
        return Panache.withTransaction(fruit::persist).replaceWith(RestResponse.status(RestResponse.Status.CREATED, fruit));
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> update(Long id, Fruit fruit) {
        if (fruit == null || fruit.name == null) {
            throw new WebApplicationException("Fruit name was not set on request.", 422);
        }

        return Panache.withTransaction( () ->
                Fruit.<Fruit>findById(id)
                        .onItem()
                        .ifNotNull()
                        .invoke(entity -> entity.name = fruit.name)
        ).onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
        .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)::build);
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(long id) {
        return Panache.withTransaction(() -> Fruit.deleteById(id))
                .map(isDeleted ->
                    isDeleted ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build()
                );
    }

    /**
     * Create a HTTP response from an exception.
     *
     * Response Example:
     *
     * <pre>
     * HTTP/1.1 422 Unprocessable Entity
     * Content-Length: 111
     * Content-Type: application/json
     *
     * {
     *     "code": 422,
     *     "error": "Fruit name was not set on request.",
     *     "exceptionType": "jakarta.ws.rs.WebApplicationException"
     * }
     * </pre>
     */
    @Provider
    public static class ErrorMapper implements ExceptionMapper<Exception> {

        @Inject
        ObjectMapper objectMapper;

        @Override
        public Response toResponse(Exception exception) {
            Throwable throwable = exception;

            int code = 500;
            if (throwable instanceof WebApplicationException) {
                code = ((WebApplicationException) exception).getResponse().getStatus();
            } else if (throwable instanceof CompositeException) {
                // This is a Mutiny exception and it happens, for example, when we try to insert a new
                // fruit but the name is already in the database
                throwable = ((CompositeException) throwable).getCause();
            } else if (throwable instanceof CompletionException ||
                    throwable instanceof HibernateException) {
                throwable = throwable.getCause();

                if (exception instanceof ConstraintViolationException) {
                    code = 422;
                } else if (throwable instanceof PersistenceException) {
                    code = 422;
                } else if (throwable instanceof CompletionException
                        && throwable.getCause() instanceof PersistentObjectException) {
                    code = 422;
                }
            }

            if (code >= 500) {
                LOGGER.error("Failed to handle request", exception);
            }

            ObjectNode exceptionJson = objectMapper.createObjectNode();
            exceptionJson.put("exceptionType", throwable.getClass().getName());
            exceptionJson.put("code", code);

            if (exception.getMessage() != null) {
                exceptionJson.put("error", throwable.getMessage());
            }

            return Response.status(code)
                    .entity(exceptionJson)
                    .build();
        }

    }

}
