package com.helpflow.supportticket.unit.observability;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.helpflow.supportticket.infrastructure.observability.TraceIdFilter;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TraceIdFilterTest {

	private TraceIdFilter filter;

	@BeforeEach
	void setUp() {
		filter = new TraceIdFilter();
		MDC.clear();
	}

	@Test
	void usesExistingTraceIdHeaderWhenPresent() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(TraceIdFilter.TRACE_ID_HEADER, "custom-trace-12345");
		MockHttpServletResponse response = new MockHttpServletResponse();
		AtomicReference<String> mdcTraceIdDuringChain = new AtomicReference<>();

		filter.doFilter(request, response, (req, res) ->
				mdcTraceIdDuringChain.set(MDC.get(TraceIdFilter.MDC_KEY)));

		assertEquals("custom-trace-12345", mdcTraceIdDuringChain.get());
		assertEquals("custom-trace-12345", response.getHeader(TraceIdFilter.TRACE_ID_HEADER));
		assertNull(MDC.get(TraceIdFilter.MDC_KEY));
	}

	@Test
	void generatesUuidWhenTraceIdHeaderIsMissing() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		AtomicReference<String> mdcTraceIdDuringChain = new AtomicReference<>();

		filter.doFilter(request, response, (req, res) ->
				mdcTraceIdDuringChain.set(MDC.get(TraceIdFilter.MDC_KEY)));

		String traceId = mdcTraceIdDuringChain.get();
		assertNotNull(traceId);
		assertDoesNotThrow(() -> UUID.fromString(traceId));
		assertEquals(traceId, response.getHeader(TraceIdFilter.TRACE_ID_HEADER));
		assertNull(MDC.get(TraceIdFilter.MDC_KEY));
	}

	@Test
	void generatesUuidWhenTraceIdHeaderIsBlank() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(TraceIdFilter.TRACE_ID_HEADER, "   ");
		MockHttpServletResponse response = new MockHttpServletResponse();
		AtomicReference<String> mdcTraceIdDuringChain = new AtomicReference<>();

		filter.doFilter(request, response, (req, res) ->
				mdcTraceIdDuringChain.set(MDC.get(TraceIdFilter.MDC_KEY)));

		String traceId = mdcTraceIdDuringChain.get();
		assertNotNull(traceId);
		assertDoesNotThrow(() -> UUID.fromString(traceId));
		assertEquals(traceId, response.getHeader(TraceIdFilter.TRACE_ID_HEADER));
		assertNull(MDC.get(TraceIdFilter.MDC_KEY));
	}

	@Test
	void cleansUpMdcEvenWhenChainThrowsException() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		assertThrows(ServletException.class, () ->
				filter.doFilter(request, response, (req, res) -> {
					assertEquals(response.getHeader(TraceIdFilter.TRACE_ID_HEADER), MDC.get(TraceIdFilter.MDC_KEY));
					throw new ServletException("Simulated filter chain failure");
				}));

		assertNull(MDC.get(TraceIdFilter.MDC_KEY));
	}
}

