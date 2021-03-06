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
import dev.profunktor.redis4cats.algebra.SortedSetCommands
import dev.profunktor.redis4cats.connection._
import dev.profunktor.redis4cats.effect.Log
import dev.profunktor.redis4cats.effects.{ Score, ScoreWithValue, ZRange }
import dev.profunktor.redis4cats.interpreter.Redis

object RedisSortedSetsDemo extends LoggerIOApp {

  import Demo._

  def program(implicit log: Log[IO]): IO[Unit] = {
    val testKey = "zztop"

    val commandsApi: Resource[IO, SortedSetCommands[IO, String, Long]] =
      for {
        uri <- Resource.liftF(RedisURI.make[IO](redisURI))
        client <- RedisClient[IO](uri)
        redis <- Redis[IO, String, Long](client, longCodec)
      } yield redis

    commandsApi
      .use { cmd =>
        for {
          _ <- cmd.zAdd(testKey, args = None, ScoreWithValue(Score(1), 1), ScoreWithValue(Score(3), 2))
          x <- cmd.zRevRangeByScore(testKey, ZRange(0, 2), limit = None)
          _ <- putStrLn(s"Score: $x")
          y <- cmd.zCard(testKey)
          _ <- putStrLn(s"Size: $y")
          z <- cmd.zCount(testKey, ZRange(0, 1))
          _ <- putStrLn(s"Count: $z")
        } yield ()
      }
  }

}
