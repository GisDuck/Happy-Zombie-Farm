package game.Happy_Zombie_Farm.api;

import game.Happy_Zombie_Farm.exception.*;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GraphqlExceptionHandler {

    @ExceptionHandler({
            NoHouseException.class,
            NoPlayerException.class,
            NotThisPlayerHouseIdException.class,
            ResourcesException.class,
            TelegramDataNotValidException.class,
            WrongSkinHouseParamException.class
    })
    public GraphQLError handleGameErrors(WrongSkinHouseParamException ex,
                                        DataFetchingEnvironment env) {
        return GraphqlErrorBuilder.newError(env)
                .message(ex.getMessage())
                .errorType(ErrorType.BAD_REQUEST)
                .build();
    }
}
