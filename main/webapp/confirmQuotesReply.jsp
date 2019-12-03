<%@page import="ds.gae.view.JSPSite"%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<% session.setAttribute("currentPage", JSPSite.CONFIRM_QUOTES_RESPONSE); %>
<% String renterEmail = (String) session.getAttribute("renterEmail"); %>

<%@include file="_header.jsp"%>

<div class="frameDiv" style="margin: 150px 150px;">
	<h2>Reply</h2>
	<div class="group">
		<p>
			An email has been sent to <%=renterEmail%>.
		</p>
	</div>
</div>

<%@include file="_footer.jsp"%>
