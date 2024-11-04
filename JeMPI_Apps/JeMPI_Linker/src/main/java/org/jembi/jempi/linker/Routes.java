package org.jembi.jempi.linker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.linker.backend.BackEnd;
import org.jembi.jempi.shared.models.ApiModels;
import org.jembi.jempi.shared.models.GlobalConstants;

import static akka.http.javadsl.server.Directives.*;
import static org.jembi.jempi.linker.MapError.mapError;
import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;

final class Routes {

   private static final Logger LOGGER = LogManager.getLogger(Routes.class);
   private static final Marshaller<Object, RequestEntity> JSON_MARSHALLER = Jackson.marshaller(OBJECT_MAPPER);

   private Routes() {
   }

   static StatusCode logHttpError(
         final StatusCode code,
         final String log) {
      LOGGER.debug("{}", log);
      return code;
   }

   private static Route handleError(final Throwable e) {
      LOGGER.error(e.getLocalizedMessage(), e);
      return mapError(new MpiServiceError.InternalError(e.getLocalizedMessage()));
   }

   static Route proxyPostCalculateScores(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(ApiModels.ApiCalculateScoresRequest.class),
                    obj -> onComplete(Ask.postCalculateScores(actorSystem, backEnd, obj), result -> {
                       if (!result.isSuccess()) {
                          return handleError(result.failed().get());
                       }
                       return complete(StatusCodes.OK, result.get(), JSON_MARSHALLER);
                    }));
   }

   static Route proxyPostCandidatesWithScore(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(ApiModels.ApiInteractionUid.class),
                    obj -> onComplete(Ask.findCandidates(actorSystem, backEnd, obj), result -> {
                       if (!result.isSuccess()) {
                          return handleError(result.failed().get());
                       }
                       return result.get()
                                    .candidates()
                                    .mapLeft(MapError::mapError)
                                    .fold(error -> error,
                                          candidateList -> complete(StatusCodes.OK,
                                                                    candidateList,
                                                                    JSON_MARSHALLER));
                    }));
   }

   static Route proxyPostCrLinkToGidUpdate(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrLinkToGidUpdateRequest.class),
                    obj -> onComplete(Ask.postCrLinkToGidUpdate(actorSystem, backEnd, obj), result -> {
                       if (!result.isSuccess()) {
                          return handleError(result.failed().get());
                       }
                       return result.get()
                                    .linkInfo()
                                    .mapLeft(MapError::mapError)
                                    .fold(error -> error,
                                          r -> complete(StatusCodes.OK,
                                                        new ApiModels.ApiCrLinkUpdateResponse(r),
                                                        JSON_MARSHALLER));
                    }));
   }

   static Route proxyPostCrLinkBySourceId(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrLinkBySourceIdRequest.class),
                    obj -> onComplete(Ask.postCrLinkBySourceId(actorSystem, backEnd, obj), result -> {
                       if (!result.isSuccess()) {
                          return handleError(result.failed().get());
                       }
                       return result.get()
                                    .linkInfo()
                                    .mapLeft(MapError::mapError)
                                    .fold(error -> error,
                                          r -> complete(StatusCodes.OK,
                                                        new ApiModels.ApiCrLinkUpdateResponse(r),
                                                        JSON_MARSHALLER));
                    }));
   }

   static Route proxyPostCrLinkBySourceIdUpdate(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrLinkBySourceIdUpdateRequest.class),
                    obj -> onComplete(Ask.postCrLinkBySourceIdUpdate(actorSystem, backEnd, obj), result -> {
                       if (!result.isSuccess()) {
                          return handleError(result.failed().get());
                       }
                       return result.get()
                                    .linkInfo()
                                    .mapLeft(MapError::mapError)
                                    .fold(error -> error,
                                          r -> complete(StatusCodes.OK,
                                                        new ApiModels.ApiCrLinkUpdateResponse(r),
                                                        JSON_MARSHALLER));
                    }));
   }

   static Route proxyGetCrCandidates(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrCandidatesRequest.class),
                    obj -> onComplete(Ask.getCrCandidates(actorSystem, backEnd, obj), result -> {
                       if (!result.isSuccess()) {
                          return handleError(result.failed().get());
                       }
                       return result.get()
                                    .goldenRecords()
                                    .mapLeft(MapError::mapError)
                                    .fold(error -> error,
                                          r -> complete(StatusCodes.OK,
                                                        new ApiModels.ApiCrCandidatesResponse(r.stream()
                                                                                               .map(ApiModels.ApiGoldenRecord::fromGoldenRecord)
                                                                                               .toList()),
                                                        JSON_MARSHALLER));
                    }));
   }

   static Route proxyGetCrFind(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrFindRequest.class),
                    obj -> onComplete(Ask.getCrFind(actorSystem, backEnd, obj), result -> {
                       if (!result.isSuccess()) {
                          return handleError(result.failed().get());
                       }
                       return result.get()
                                    .goldenRecords()
                                    .mapLeft(MapError::mapError)
                                    .fold(error -> error,
                                          r -> complete(StatusCodes.OK,
                                                        new ApiModels.ApiCrCandidatesResponse(
                                                              r.stream()
                                                               .map(ApiModels.ApiGoldenRecord::fromGoldenRecord)
                                                               .toList()),
                                                        JSON_MARSHALLER));
                    }));
   }

   static Route proxyPostCrRegister(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.ApiCrRegisterRequest.class),
                    obj -> onComplete(Ask.postCrRegister(actorSystem, backEnd, obj), result -> {
                       if (!result.isSuccess()) {
                          return handleError(result.failed().get());
                       }
                       return result.get()
                                    .linkInfo()
                                    .mapLeft(MapError::mapError)
                                    .fold(error -> error,
                                          r -> complete(StatusCodes.OK,
                                                        new ApiModels.ApiCrRegisterResponse(r),
                                                        JSON_MARSHALLER));
                    }));
   }

   static Route proxyPostLinkInteraction(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(OBJECT_MAPPER, ApiModels.LinkInteractionSyncBody.class),
                    obj -> onComplete(Ask.postLinkInteraction(actorSystem, backEnd, obj), result -> {
                       if (!result.isSuccess()) {
                          return handleError(result.failed().get());
                       }
                       return result.get()
                                    .data()
                                    .mapLeft(MapError::mapError)
                                    .fold(error -> error,
                                          r -> complete(StatusCodes.OK,
                                                        new ApiModels.ApiExtendedLinkInfo(r.stan(),
                                                                                          r.linkInfo(),
                                                                                          r.externalLinkCandidateList()),
                                                        JSON_MARSHALLER));
                    }));
   }

   static Route proxyPostCrUpdateField(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(ApiModels.ApiCrUpdateFieldsRequest.class),
                    obj -> onComplete(Ask.postCrUpdateField(actorSystem, backEnd, obj), result -> {
                       if (!result.isSuccess()) {
                          return handleError(result.failed().get());
                       }
                       return result.get()
                                    .response()
                                    .mapLeft(MapError::mapError)
                                    .fold(error -> error,
                                          updateFieldResponse -> complete(StatusCodes.OK,
                                                                          new ApiModels.ApiCrUpdateFieldsResponse(
                                                                                updateFieldResponse.goldenId(),
                                                                                updateFieldResponse.updated(),
                                                                                updateFieldResponse.failed()),
                                                                          JSON_MARSHALLER));
                    }));
   }

   static Route proxyPostCivilRecord(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return entity(Jackson.unmarshaller(ApiModels.ApiCivilRecordRequest.class),
                    obj -> onComplete(Ask.postCivilRecord(actorSystem, backEnd, obj), result -> {
                       if (!result.isSuccess()) {
                          return handleError(result.failed().get());
                       }
                       return result.get()
                                    .response()
                                    .mapLeft(MapError::mapError)
                                    .fold(error -> error,
                                          apiCivilRecordResponse -> complete(StatusCodes.OK,
                                                                             apiCivilRecordResponse,
                                                                             JSON_MARSHALLER));
                    }));
   }


   static Route createRoute(
         final ActorSystem<Void> actorSystem,
         final ActorRef<BackEnd.Request> backEnd) {
      return pathPrefix("JeMPI",
                        () -> concat(post(() -> concat(path(GlobalConstants.SEGMENT_PROXY_POST_CR_LINK,
                                                            () -> proxyPostLinkInteraction(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_SCORES,
                                                            () -> proxyPostCalculateScores(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CR_CANDIDATES,
                                                            () -> proxyGetCrCandidates(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CR_FIND,
                                                            () -> proxyGetCrFind(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CR_REGISTER,
                                                            () -> proxyPostCrRegister(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CR_LINK_TO_GID_UPDATE,
                                                            () -> proxyPostCrLinkToGidUpdate(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CR_LINK_BY_SOURCE_ID,
                                                            () -> proxyPostCrLinkBySourceId(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CR_LINK_BY_SOURCE_ID_UPDATE,
                                                            () -> proxyPostCrLinkBySourceIdUpdate(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CANDIDATE_GOLDEN_RECORDS,
                                                            () -> proxyPostCandidatesWithScore(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CR_UPDATE_FIELDS,
                                                            () -> proxyPostCrUpdateField(actorSystem, backEnd)),
                                                       path(GlobalConstants.SEGMENT_PROXY_POST_CIVIL_RECORD,
                                                            () -> proxyPostCivilRecord(actorSystem, backEnd))))));
   }

}
