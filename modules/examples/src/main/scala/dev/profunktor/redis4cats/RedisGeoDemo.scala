/*
 * Copyright 2018-2020 ProfunKtor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.profunktor.redis4cats

import cats.effect.{ IO, Resource }
import dev.profunktor.redis4cats.algebra.GeoCommands
import dev.profunktor.redis4cats.connection._
import dev.profunktor.redis4cats.effect.Log
import dev.profunktor.redis4cats.effects._
import dev.profunktor.redis4cats.interpreter.Redis
import io.lettuce.core.GeoArgs

object RedisGeoDemo extends LoggerIOApp {

  import Demo._

  def program(implicit log: Log[IO]): IO[Unit] = {
    val testKey = "location"

    val commandsApi: Resource[IO, GeoCommands[IO, String, String]] =
      for {
        uri <- Resource.liftF(RedisURI.make[IO](redisURI))
        client <- RedisClient[IO](uri)
        redis <- Redis[IO, String, String](client, stringCodec)
      } yield redis

    val _BuenosAires  = GeoLocation(Longitude(-58.3816), Latitude(-34.6037), "Buenos Aires")
    val _RioDeJaneiro = GeoLocation(Longitude(-43.1729), Latitude(-22.9068), "Rio de Janeiro")
    val _Montevideo   = GeoLocation(Longitude(-56.164532), Latitude(-34.901112), "Montevideo")
    val _Tokyo        = GeoLocation(Longitude(139.6917), Latitude(35.6895), "Tokyo")

    commandsApi
      .use { cmd =>
        for {
          _ <- cmd.geoAdd(testKey, _BuenosAires)
          _ <- cmd.geoAdd(testKey, _RioDeJaneiro)
          _ <- cmd.geoAdd(testKey, _Montevideo)
          _ <- cmd.geoAdd(testKey, _Tokyo)
          x <- cmd.geoDist(testKey, _BuenosAires.value, _Tokyo.value, GeoArgs.Unit.km)
          _ <- putStrLn(s"Distance from ${_BuenosAires.value} to Tokyo: $x km")
          y <- cmd.geoPos(testKey, _RioDeJaneiro.value)
          _ <- putStrLn(s"Geo Pos of ${_RioDeJaneiro.value}: ${y.headOption}")
          z <- cmd.geoRadius(testKey, GeoRadius(_Montevideo.lon, _Montevideo.lat, Distance(10000.0)), GeoArgs.Unit.km)
          _ <- putStrLn(s"Geo Radius in 1000 km: $z")
        } yield ()
      }
  }

}
