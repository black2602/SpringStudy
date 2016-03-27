package springbook.annotation;

import org.springframework.context.annotation.Import;

import springbook.SqlServiceContext;

@Import(value=SqlServiceContext.class)
public @interface EnableSqlService {

}
