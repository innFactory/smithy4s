/*
 *  Copyright 2021-2024 Disney Streaming
 *
 *  Licensed under the Tomorrow Open Source Technology License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     https://disneystreaming.github.io/TOST-1.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package smithy4s

package object codecs {

  type BlobEncoder[-A] = Encoder[Blob, A]
  object BlobEncoder {
    val noop: BlobEncoder[Any] = Encoder.static(Blob.empty)
  }

  type BlobDecoder[+A] = Decoder[Either[PayloadError, *], Blob, A]
  object BlobDecoder {
    val noop: BlobDecoder[Nothing] = Decoder.lift(_ =>
      Left(PayloadError(PayloadPath.root, "nothing", "always failing"))
    )
  }

  type PayloadDecoder[A] = Decoder[Either[PayloadError, *], Blob, A]
  type PayloadEncoder[A] = Encoder[Blob, A]

}
