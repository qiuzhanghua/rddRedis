package com.example.rddredis

import com.redislabs.provider.redis.ReadWriteConfig
import com.redislabs.provider.redis.RedisConfig
import com.redislabs.provider.redis.RedisContext
import org.apache.spark.SparkConf
import org.apache.spark.api.java.JavaRDD
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.SparkSession
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import scala.Tuple2
import scala.reflect.`ClassManifestFactory$`
import java.util.*


@SpringBootApplication
class RddRedisApplication {

    @Bean
    fun sparkConfig(): SparkConf {
        return SparkConf()
            .setAppName("RddRedisApplication")
            .setMaster("local[*]")
            .set("spark.redis.host", "localhost")
            .set("spark.redis.port", "6379")
        // optional redis AUTH password
        // .set("spark.redis.auth", "passwd")
    }

    @Bean
    fun redisConfig(): RedisConfig {
        return RedisConfig.fromSparkConf(sparkConfig())
    }

    @Bean
    fun redisContext(conf: SparkConf): RedisContext {
        return RedisContext(jsc().sc())
    }

    @Bean
    fun readWriteConfig(): ReadWriteConfig {
        return ReadWriteConfig.fromSparkConf(sparkConfig())
    }

    @Bean(destroyMethod = "close")
    fun jsc(): JavaSparkContext {
        return JavaSparkContext(sparkConfig())
    }

    @Bean(destroyMethod = "close")
    fun spark(): SparkSession {
        return SparkSession.builder()
            .config(sparkConfig())
            .orCreate
    }

    @Bean
    fun applicationRunner(
        spark: SparkSession,
        jsc: JavaSparkContext,
        redisConfig: RedisConfig,
        redisContext: RedisContext,
        readWriteConfig: ReadWriteConfig
    ): ApplicationRunner {
        return ApplicationRunner {
            val people = listOf(
                Person("1", "John", "Doe", Date()),
                Person("2", "Jane", "Doe", Date())
            )

            val df = spark.createDataFrame(people, Person::class.java)
//            println("===========")
            df.show()
            df.printSchema()
            df.write()
                .format("org.apache.spark.sql.redis")
                .option("table", "people")
                .option("key.column", "id")
                .mode(SaveMode.Overwrite)
                .save()

            val df2 = spark.read()
                .format("org.apache.spark.sql.redis")
                .option("infer.schema", "true")
                .option("key.column", "id")
                .option("keys.pattern", "people:*")
                .load()
            df2.show()
//            println("===========")
//            println(df2.first())
//            println("===========")

            val rdd = JavaRDD.fromRDD(
                redisContext
                    .fromRedisHash(
                        "people:1",
                        3,
                        redisConfig,
                        readWriteConfig
                    ),
                `ClassManifestFactory$`.`MODULE$`.classType(Tuple2::class.java)
            )
                .collect()
            rdd.forEach { println(it) }
        }
    }

}

fun main(args: Array<String>) {
    runApplication<RddRedisApplication>(*args)
}

data class Person(
    val id: String,
    val firstName: String,
    val lastName: String,
    val birth: Date
)

@RestController
class HelloController {
    @RequestMapping("/")
    fun index(): String {
        return "Hello World!"
    }
}
