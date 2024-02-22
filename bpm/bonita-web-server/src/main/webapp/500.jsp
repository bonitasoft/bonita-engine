<%-- Copyright (C) 2022 BonitaSoft S.A.
 BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 2.0 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. --%>
<%@page contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html"><head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
  <title>Error 500</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.min.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/error-style.css">
 </head> 
 <body>
   <div class="main-container">
    <div class="error-title">
     <h1 class="text-danger">Sorry...</h1>
     <h2 class="text-danger">It's not you, it's us.</h2>
    </div>
    <div class="illustration-container">
     <img alt="500 error" src="${pageContext.request.contextPath}/images/500.svg">
    </div>
    <div class="error-message">
     <h2 class="text-primary">The server is currently unable to handle this request. Please be patient or try again later.</h2>
     <p class="text-primary">500 Internal server error</p>
    </div>
   </div>
  </div>  
 </body>
</html>
