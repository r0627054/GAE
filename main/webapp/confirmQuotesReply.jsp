<%@page import="ds.gae.view.JSPSite"%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<% session.setAttribute("currentPage", JSPSite.CONFIRM_QUOTES_RESPONSE); %>
<% String renterEmail = (String) session.getAttribute("renterEmail"); %>
<% String orderId = (String) session.getAttribute("orderId"); %>

<%@include file="_header.jsp"%>

<div class="frameDiv" style="margin: 150px 150px;">
	<h2>Reply</h2>
	<div class="group">
		<p>
			ORDER ID: <%=orderId%>
		</p>
		<p>
			An email will be sent to <%=renterEmail%> with confirmation of the reservation.
		</p>
	</div>
</div>

<%@include file="_footer.jsp"%>
