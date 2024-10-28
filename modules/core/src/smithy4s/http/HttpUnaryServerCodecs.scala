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
package http

import smithy4s.capability.MonadThrowLike
import smithy4s.codecs.BlobDecoder
import smithy4s.codecs.BlobEncoder
import smithy4s.codecs.Decoder
import smithy4s.codecs.PayloadError
import smithy4s.codecs.Writer
import smithy4s.schema.OperationSchema
import smithy4s.server.UnaryServerCodecs
import smithy4s.schema.Compiler
import smithy4s.schema.Compilation

// scalafmt: {maxColumn = 120}
object HttpUnaryServerCodecs {

  def builder[F[_]](implicit F: MonadThrowLike[F]): Builder[F, HttpRequest[Blob], HttpResponse[Blob]] =
    HttpUnaryClientCodecsBuilderImpl[F, HttpRequest[Blob], HttpResponse[Blob]](
      baseResponse = _ => F.raiseError(new Exception("Undefined base response")),
      requestBodyDecoders = Compiler.covariantStatic(BlobDecoder.noop),
      successResponseBodyEncoders = Compiler.contravariantStatic(BlobEncoder.noop),
      errorResponseBodyEncoders = Compiler.contravariantStatic(BlobEncoder.noop),
      metadataEncoders = None,
      metadataDecoders = None,
      rawStringsAndBlobPayloads = false,
      writeEmptyStructs = _ => false,
      errorTypeHeaders = Nil,
      responseMediaType = "text/plain",
      requestTransformation = F.pure(_),
      responseTransformation = F.pure(_)
    )

  trait Builder[F[_], Request, Response] {
    def withBaseResponse(f: OperationSchema[_, _, _, _, _] => F[HttpResponse[Blob]]): Builder[F, Request, Response]
    def withBodyDecoders(decoders: Compiler[BlobDecoder]): Builder[F, Request, Response]
    def withSuccessBodyEncoders(decoders: Compiler[BlobEncoder]): Builder[F, Request, Response]
    def withErrorBodyEncoders(encoders: Compiler[BlobEncoder]): Builder[F, Request, Response]
    def withMetadataEncoders(encoders: Metadata.Encoder.Compiler): Builder[F, Request, Response]
    def withMetadataDecoders(decoders: Metadata.Decoder.Compiler): Builder[F, Request, Response]
    def withRawStringsAndBlobsPayloads: Builder[F, Request, Response]
    def withWriteEmptyStructs(cond: Schema[_] => Boolean): Builder[F, Request, Response]
    def withResponseMediaType(mediaType: String): Builder[F, Request, Response]
    def withErrorTypeHeaders(headerNames: String*): Builder[F, Request, Response]
    def withRequestTransformation[Request0](f: Request0 => F[Request]): Builder[F, Request0, Response]
    def withResponseTransformation[Response1](f: Response => F[Response1]): Builder[F, Request, Response1]
    def build(): UnaryServerCodecs.Make[F, Request, Response]
  }

  private case class HttpUnaryClientCodecsBuilderImpl[F[_], Request, Response](
      baseResponse: OperationSchema[_, _, _, _, _] => F[HttpResponse[Blob]],
      requestBodyDecoders: Compiler[BlobDecoder],
      successResponseBodyEncoders: Compiler[BlobEncoder],
      errorResponseBodyEncoders: Compiler[BlobEncoder],
      metadataEncoders: Option[Metadata.Encoder.Compiler],
      metadataDecoders: Option[Metadata.Decoder.Compiler],
      rawStringsAndBlobPayloads: Boolean,
      writeEmptyStructs: Schema[_] => Boolean,
      responseMediaType: String,
      errorTypeHeaders: List[String],
      requestTransformation: Request => F[HttpRequest[Blob]],
      responseTransformation: HttpResponse[Blob] => F[Response]
  )(implicit F: MonadThrowLike[F])
      extends Builder[F, Request, Response] {

    def withBaseResponse(f: OperationSchema[_, _, _, _, _] => F[HttpResponse[Blob]]): Builder[F, Request, Response] =
      copy(baseResponse = f)
    def withBodyDecoders(decoders: Compiler[BlobDecoder]): Builder[F, Request, Response] =
      copy(requestBodyDecoders = decoders)
    def withSuccessBodyEncoders(encoders: Compiler[BlobEncoder]): Builder[F, Request, Response] =
      copy(successResponseBodyEncoders = encoders)
    def withErrorBodyEncoders(encoders: Compiler[BlobEncoder]): Builder[F, Request, Response] =
      copy(errorResponseBodyEncoders = encoders)
    def withMetadataEncoders(encoders: Metadata.Encoder.Compiler): Builder[F, Request, Response] =
      copy(metadataEncoders = Some(encoders))
    def withMetadataDecoders(decoders: Metadata.Decoder.Compiler): Builder[F, Request, Response] =
      copy(metadataDecoders = Some(decoders))
    def withRawStringsAndBlobsPayloads: Builder[F, Request, Response] =
      copy(rawStringsAndBlobPayloads = true)
    def withWriteEmptyStructs(cond: Schema[_] => Boolean): Builder[F, Request, Response] =
      copy(writeEmptyStructs = cond)
    def withResponseMediaType(mediaType: String): Builder[F, Request, Response] =
      copy(responseMediaType = mediaType)
    def withErrorTypeHeaders(headerNames: String*): Builder[F, Request, Response] =
      copy(errorTypeHeaders = headerNames.toList)

    def withRequestTransformation[Request0](f: Request0 => F[Request]): Builder[F, Request0, Response] =
      copy(requestTransformation = f.andThen(F.flatMap(_)(requestTransformation)))
    def withResponseTransformation[Response1](f: Response => F[Response1]): Builder[F, Request, Response1] =
      copy(responseTransformation = responseTransformation.andThen(F.flatMap(_)(f)))

    def build(): UnaryServerCodecs.Make[F, Request, Response] = {
      val setBody: HttpResponse.Writer[Blob, Blob] = Writer.lift((res, blob) => res.copy(body = blob))
      val setBodyK = smithy4s.codecs.Encoder.pipeToWriterK[HttpResponse[Blob], Blob](setBody)

      val mediaTypeWriters = new Compiler[HttpResponse.Writer[Blob, *]] {
        def apply[A](schema: Schema[A]): Compilation[HttpResponse.Writer[Blob, A]] = {
          val mt = if (rawStringsAndBlobPayloads) {
            HttpMediaType.fromSchema(schema).map(_.value).getOrElse(responseMediaType)
          } else responseMediaType
          Compilation.pure {
            new HttpResponse.Writer[Blob, A] {
              def write(request: HttpResponse[Blob], value: A): HttpResponse[Blob] =
                if (request.body.isEmpty) request
                else request.withContentType(mt)
            }
          }
        }
      }

      def responseEncoders(blobEncoders: Compiler[BlobEncoder]) = {
        val httpBodyWriters: Compiler[HttpResponse.Writer[Blob, *]] = if (rawStringsAndBlobPayloads) {
          val finalBodyEncoders = Compiler
            .getOrElse(smithy4s.codecs.StringAndBlobCodecs.encoders, successResponseBodyEncoders)
          finalBodyEncoders.mapK(setBodyK)
        } else successResponseBodyEncoders.mapK(setBodyK)

        val httpMediaWriters: Compiler[HttpResponse.Writer[Blob, *]] =
          Writer.combineCompilers(httpBodyWriters, mediaTypeWriters)

        metadataEncoders match {
          case Some(mEncoders) =>
            HttpResponse.Encoder.restSchemaCompiler(mEncoders, httpMediaWriters, writeEmptyStructs)
          case None => httpMediaWriters
        }
      }

      val inputDecoders: Compiler[HttpRequest.Decoder[F, Blob, *]] = {
        val httpBodyDecoders: Compiler[Decoder[F, Blob, *]] = {
          val decoders: Compiler[BlobDecoder] = if (rawStringsAndBlobPayloads) {
            Compiler
              .getOrElse(smithy4s.codecs.StringAndBlobCodecs.decoders, requestBodyDecoders)
          } else requestBodyDecoders
          decoders.mapK(
            Decoder
              .of[Blob]
              .liftPolyFunction(
                MonadThrowLike
                  .liftEitherK[F, PayloadError]
                  .andThen(HttpContractError.fromPayloadErrorK[F])
              )
          )
        }

        metadataDecoders match {
          case Some(mDecoders) => HttpRequest.Decoder.restSchemaCompiler(mDecoders, httpBodyDecoders, None)
          case None            => httpBodyDecoders.mapK(HttpRequest.extractBody[F, Blob])
        }
      }

      val outputEncoders = responseEncoders(successResponseBodyEncoders)
      val errorEncoders = responseEncoders(errorResponseBodyEncoders)
      val compiledHttpContractErrorWriters = errorEncoders(HttpContractError.schema)

      new UnaryServerCodecs.Make[F, Request, Response] {

        def apply[I, E, O, SI, SO](
            endpoint: OperationSchema[I, E, O, SI, SO]
        ): Compilation[UnaryServerCodecs[F, Request, Response, I, E, O]] = {
          val compiledOutputWriter = endpoint.hints.get(smithy.api.Http) match {
            case Some(http) =>
              val preProcess: HttpResponse[Blob] => HttpResponse[Blob] =
                _.withStatusCode(http.code)
              // TODO : add unit-tests for this
              val postProcessResponse: HttpResponse[Blob] => HttpResponse[Blob] =
                if (http.code == 204 || http.method.value.toLowerCase == "head")
                  _.withBody(Blob.empty)
                else identity
              outputEncoders(endpoint.output).map(_.compose(preProcess).andThen(postProcessResponse))
            case None => outputEncoders(endpoint.output)
          }
          val compiledErrorWriter =
            Compilation.pure(HttpResponse.Encoder.forError(errorTypeHeaders, endpoint.error, errorEncoders))
          val compiledInputDecoder: Compilation[HttpRequest.Decoder[F, Blob, I]] = inputDecoders(endpoint.input)
          val base = baseResponse(endpoint)

          Compilation.map4(
            compiledInputDecoder,
            compiledErrorWriter,
            compiledHttpContractErrorWriters,
            compiledOutputWriter
          ) { (inputDecoder, errorW, httpContractErrorW, outputW) =>
            def httpContractErrorEncoder(e: HttpContractError) =
              F.map(base)(httpContractErrorW.write(_, e).withStatusCode(400))
            def throwableEncoders(throwable: Throwable): F[HttpResponse[Blob]] =
              throwable match {
                case e: HttpContractError => httpContractErrorEncoder(e)
                case e                    => F.raiseError(e)
              }
            def encodeOutput(o: O) = F.map(base)(outputW.write(_, o))
            def encodeError(e: E) = F.map(base)(errorW.write(_, e))
            new UnaryServerCodecs(inputDecoder.decode, encodeError, throwableEncoders, encodeOutput)
              .transformRequest(requestTransformation)
              .transformResponse(responseTransformation)
          }

        }

      }
    }
  }

}
