// Copyright © 2020 Amazon Web Services
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// package org.opengroup.osdu.partition.provider.aws.security;

// import java.io.IOException;
// import java.util.Collections;
// import java.util.function.Function;
// import java.util.logging.Logger;
// import java.util.stream.Collectors;

// import javax.servlet.FilterChain;
// import javax.servlet.ServletException;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;

// import org.opengroup.osdu.core.common.model.http.AppException;
// import org.opengroup.osdu.core.common.model.http.DpsHeaders;
// import org.springframework.http.HttpHeaders;
// import org.springframework.lang.NonNull;
// import org.springframework.security.authentication.AnonymousAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.util.MultiValueMap;
// import org.springframework.web.filter.OncePerRequestFilter;
// import org.springframework.web.servlet.HandlerExceptionResolver;
// import org.opengroup.osdu.core.common.model.entitlements.Groups;

// public class AuthenticationRequestFilter extends OncePerRequestFilter {

//     private static Logger logger = Logger.getLogger(AuthenticationRequestFilter.class.getName());

//     private final HandlerExceptionResolver handlerExceptionResolver;

//     public AuthenticationRequestFilter(HandlerExceptionResolver handlerExceptionResolver) {
//         this.handlerExceptionResolver = handlerExceptionResolver;
//     }

//     @Override
//     protected void doFilterInternal(@NonNull HttpServletRequest httpServletRequest,
//                                     @NonNull HttpServletResponse httpServletResponse,
//                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
//         MultiValueMap<String, String> requestHeaders = httpHeaders(httpServletRequest);
//         DpsHeaders dpsHeaders = DpsHeaders.createFromEntrySet(requestHeaders.entrySet());
//         dpsHeaders.addCorrelationIdIfMissing();

//         try {
          
//             // String message = String.format("User authenticated | User: %s", groups.getMemberEmail());
//             authenticate(null);
//             // logger.info(message);
//             filterChain.doFilter(httpServletRequest, httpServletResponse);
//         }
//         //  catch (EntitlementsException e) {
//         //     String message = String.format("User not authenticated. Response: %s", e.getHttpResponse());
//         //     logger.warning(message);
//         //     AppException unauthorized = AppException.createUnauthorized("Error: " + e.getMessage());
//         //     handlerExceptionResolver.resolveException(httpServletRequest, httpServletResponse, null, unauthorized);
//         // }
//         catch (NullPointerException e) {
//             String message = String.format("User not authenticated. Null pointer exception: %s", e.getMessage());
//             logger.warning(message);
//             AppException unauthorized = AppException.createUnauthorized("Error: " + e.getMessage());
//             handlerExceptionResolver.resolveException(httpServletRequest, httpServletResponse, null, unauthorized);
//         }

//     }

//     private void authenticate(Groups groups) {
//         OsduAuthentication authentication = new OsduAuthentication(groups, emptyList());
//         authentication.setAuthenticated(true);
//         SecurityContextHolder.getContext().setAuthentication(authentication);
//     }

//     private HttpHeaders httpHeaders(@NonNull HttpServletRequest httpRequest) {
//         return Collections
//                 .list(httpRequest.getHeaderNames())
//                 .stream()
//                 .collect(Collectors.toMap(
//                         Function.identity(),
//                         h -> Collections.list(httpRequest.getHeaders(h)),
//                         (oldValue, newValue) -> newValue,
//                         HttpHeaders::new
//                 ));
//     }
// }
