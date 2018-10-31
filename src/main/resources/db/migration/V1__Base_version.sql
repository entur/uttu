--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.2
-- Dumped by pg_dump version 9.5.2

-- Started on 2018-10-30 13:41:52 CET

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 1 (class 3079 OID 12395)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner:
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 3855 (class 0 OID 0)
-- Dependencies: 1
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner:
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- TOC entry 2 (class 3079 OID 7795458)
-- Name: postgis; Type: EXTENSION; Schema: -; Owner:
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- TOC entry 3856 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner:
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry, geography, and raster spatial types and functions';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 222 (class 1259 OID 7889037)
-- Name: booking_arrangement; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE booking_arrangement (
    pk bigint NOT NULL,
    changed timestamp without time zone NOT NULL,
    changed_by character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    created_by character varying(255) NOT NULL,
    version bigint NOT NULL,
    book_when character varying(255),
    booking_access character varying(255),
    booking_note character varying(255),
    latest_booking_time timestamp without time zone,
    minimum_booking_period bigint,
    booking_contact_pk bigint
);


ALTER TABLE booking_arrangement OWNER TO nabu;

--
-- TOC entry 223 (class 1259 OID 7889045)
-- Name: booking_arrangement_booking_methods; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE booking_arrangement_booking_methods (
    booking_arrangement_pk bigint NOT NULL,
    booking_methods character varying(255)
);


ALTER TABLE booking_arrangement_booking_methods OWNER TO nabu;

--
-- TOC entry 224 (class 1259 OID 7889048)
-- Name: booking_arrangement_buy_when; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE booking_arrangement_buy_when (
    booking_arrangement_pk bigint NOT NULL,
    buy_when character varying(255)
);


ALTER TABLE booking_arrangement_buy_when OWNER TO nabu;

--
-- TOC entry 201 (class 1259 OID 7888995)
-- Name: booking_arrangement_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE booking_arrangement_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE booking_arrangement_seq OWNER TO nabu;

--
-- TOC entry 200 (class 1259 OID 7853790)
-- Name: code_space_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE code_space_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE code_space_seq OWNER TO nabu;

--
-- TOC entry 225 (class 1259 OID 7889051)
-- Name: codespace; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE codespace (
    pk bigint NOT NULL,
    changed timestamp without time zone NOT NULL,
    changed_by character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    created_by character varying(255) NOT NULL,
    version bigint NOT NULL,
    xmlns character varying(255) NOT NULL,
    xmlns_url character varying(255) NOT NULL
);


ALTER TABLE codespace OWNER TO nabu;

--
-- TOC entry 202 (class 1259 OID 7888997)
-- Name: codespace_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE codespace_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE codespace_seq OWNER TO nabu;

--
-- TOC entry 226 (class 1259 OID 7889059)
-- Name: contact; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE contact (
    pk bigint NOT NULL,
    changed timestamp without time zone NOT NULL,
    changed_by character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    created_by character varying(255) NOT NULL,
    version bigint NOT NULL,
    contact_person character varying(255),
    email character varying(255),
    further_details character varying(255),
    phone character varying(255),
    url character varying(255)
);


ALTER TABLE contact OWNER TO nabu;

--
-- TOC entry 203 (class 1259 OID 7888999)
-- Name: contact_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE contact_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE contact_seq OWNER TO nabu;

--
-- TOC entry 227 (class 1259 OID 7889067)
-- Name: day_type; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE day_type (
    pk bigint NOT NULL,
    changed timestamp without time zone NOT NULL,
    changed_by character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    created_by character varying(255) NOT NULL,
    version bigint NOT NULL,
    netex_id character varying(255) NOT NULL,
    provider_pk bigint NOT NULL
);


ALTER TABLE day_type OWNER TO nabu;

--
-- TOC entry 230 (class 1259 OID 7889081)
-- Name: day_type_assignment; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE day_type_assignment (
    pk bigint NOT NULL,
    changed timestamp without time zone NOT NULL,
    changed_by character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    created_by character varying(255) NOT NULL,
    version bigint NOT NULL,
    available boolean,
    date timestamp without time zone,
    operating_period_pk bigint
);


ALTER TABLE day_type_assignment OWNER TO nabu;

--
-- TOC entry 205 (class 1259 OID 7889003)
-- Name: day_type_assignment_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE day_type_assignment_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE day_type_assignment_seq OWNER TO nabu;

--
-- TOC entry 229 (class 1259 OID 7889078)
-- Name: day_type_day_type_assignments; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE day_type_day_type_assignments (
    day_type_pk bigint NOT NULL,
    day_type_assignments_pk bigint NOT NULL
);


ALTER TABLE day_type_day_type_assignments OWNER TO nabu;

--
-- TOC entry 228 (class 1259 OID 7889075)
-- Name: day_type_days_of_week; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE day_type_days_of_week (
    day_type_pk bigint NOT NULL,
    days_of_week character varying(255)
);


ALTER TABLE day_type_days_of_week OWNER TO nabu;

--
-- TOC entry 199 (class 1259 OID 7833487)
-- Name: day_type_operating_periods; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE day_type_operating_periods (
    day_type_pk bigint NOT NULL,
    operating_periods_pk bigint NOT NULL
);


ALTER TABLE day_type_operating_periods OWNER TO nabu;

--
-- TOC entry 204 (class 1259 OID 7889001)
-- Name: day_type_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE day_type_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE day_type_seq OWNER TO nabu;

--
-- TOC entry 231 (class 1259 OID 7889089)
-- Name: destination_display; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE destination_display (
    pk bigint NOT NULL,
    changed timestamp without time zone NOT NULL,
    changed_by character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    created_by character varying(255) NOT NULL,
    version bigint NOT NULL,
    netex_id character varying(255) NOT NULL,
    front_text character varying(255) NOT NULL,
    provider_pk bigint NOT NULL
);


ALTER TABLE destination_display OWNER TO nabu;

--
-- TOC entry 206 (class 1259 OID 7889005)
-- Name: destination_display_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE destination_display_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE destination_display_seq OWNER TO nabu;

--
-- TOC entry 232 (class 1259 OID 7889097)
-- Name: export; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE export (
    pk bigint NOT NULL,
    changed timestamp without time zone NOT NULL,
    changed_by character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    created_by character varying(255) NOT NULL,
    version bigint NOT NULL,
    netex_id character varying(255) NOT NULL,
    export_status character varying(255) NOT NULL,
    from_date timestamp without time zone,
    name character varying(255),
    to_date timestamp without time zone,
    provider_pk bigint NOT NULL
);


ALTER TABLE export OWNER TO nabu;

--
-- TOC entry 234 (class 1259 OID 7889108)
-- Name: export_message; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE export_message (
    pk bigint NOT NULL,
    message character varying(4000) NOT NULL,
    severity character varying(255) NOT NULL
);


ALTER TABLE export_message OWNER TO nabu;

--
-- TOC entry 208 (class 1259 OID 7889009)
-- Name: export_message_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE export_message_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE export_message_seq OWNER TO nabu;

--
-- TOC entry 233 (class 1259 OID 7889105)
-- Name: export_messages; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE export_messages (
    export_pk bigint NOT NULL,
    messages_pk bigint NOT NULL
);


ALTER TABLE export_messages OWNER TO nabu;

--
-- TOC entry 207 (class 1259 OID 7889007)
-- Name: export_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE export_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE export_seq OWNER TO nabu;

--
-- TOC entry 235 (class 1259 OID 7889116)
-- Name: flexible_area; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE flexible_area (
    pk bigint NOT NULL,
    changed timestamp without time zone NOT NULL,
    changed_by character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    created_by character varying(255) NOT NULL,
    version bigint NOT NULL,
    polygon_id bigint NOT NULL
);


ALTER TABLE flexible_area OWNER TO nabu;

--
-- TOC entry 209 (class 1259 OID 7889011)
-- Name: flexible_area_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE flexible_area_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE flexible_area_seq OWNER TO nabu;

--
-- TOC entry 236 (class 1259 OID 7889124)
-- Name: flexible_line; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE flexible_line (
    pk bigint NOT NULL,
    changed timestamp without time zone NOT NULL,
    changed_by character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    created_by character varying(255) NOT NULL,
    version bigint NOT NULL,
    netex_id character varying(255) NOT NULL,
    description character varying(255),
    name character varying(255),
    private_code character varying(255),
    short_name character varying(255),
    flexible_line_type character varying(255) NOT NULL,
    operator_ref character varying(255),
    public_code character varying(255),
    transport_mode character varying(255) NOT NULL,
    transport_submode character varying(255) NOT NULL,
    provider_pk bigint NOT NULL,
    booking_arrangement_pk bigint,
    network_pk bigint NOT NULL
);


ALTER TABLE flexible_line OWNER TO nabu;

--
-- TOC entry 237 (class 1259 OID 7889132)
-- Name: flexible_line_notices; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE flexible_line_notices (
    flexible_line_pk bigint NOT NULL,
    notices_pk bigint NOT NULL
);


ALTER TABLE flexible_line_notices OWNER TO nabu;

--
-- TOC entry 210 (class 1259 OID 7889013)
-- Name: flexible_line_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE flexible_line_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE flexible_line_seq OWNER TO nabu;

--
-- TOC entry 238 (class 1259 OID 7889135)
-- Name: flexible_stop_place; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE flexible_stop_place (
    pk bigint NOT NULL,
    changed timestamp without time zone NOT NULL,
    changed_by character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    created_by character varying(255) NOT NULL,
    version bigint NOT NULL,
    netex_id character varying(255) NOT NULL,
    description character varying(255),
    name character varying(255),
    private_code character varying(255),
    short_name character varying(255),
    transport_mode character varying(255) NOT NULL,
    provider_pk bigint NOT NULL,
    flexible_area_pk bigint,
    hail_and_ride_area_pk bigint
);


ALTER TABLE flexible_stop_place OWNER TO nabu;

--
-- TOC entry 211 (class 1259 OID 7889015)
-- Name: flexible_stop_place_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE flexible_stop_place_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE flexible_stop_place_seq OWNER TO nabu;

--
-- TOC entry 239 (class 1259 OID 7889143)
-- Name: hail_and_ride_area; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE hail_and_ride_area (
    pk bigint NOT NULL,
    changed timestamp without time zone NOT NULL,
    changed_by character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    created_by character varying(255) NOT NULL,
    version bigint NOT NULL,
    end_quay_ref character varying(255) NOT NULL,
    start_quay_ref character varying(255) NOT NULL
);


ALTER TABLE hail_and_ride_area OWNER TO nabu;

--
-- TOC entry 212 (class 1259 OID 7889017)
-- Name: hail_and_ride_area_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE hail_and_ride_area_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE hail_and_ride_area_seq OWNER TO nabu;

--
-- TOC entry 198 (class 1259 OID 7802918)
-- Name: job; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE job (
    pk bigint NOT NULL,
    changed timestamp without time zone NOT NULL,
    changed_by character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    created_by character varying(255) NOT NULL,
    version bigint NOT NULL,
    netex_id character varying(255) NOT NULL,
    completed timestamp without time zone,
    provider_pk bigint NOT NULL
);


ALTER TABLE job OWNER TO nabu;

--
-- TOC entry 197 (class 1259 OID 7802886)
-- Name: job_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE job_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE job_seq OWNER TO nabu;

--
-- TOC entry 240 (class 1259 OID 7889151)
-- Name: journey_pattern; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE journey_pattern (
    pk bigint NOT NULL,
    changed timestamp without time zone NOT NULL,
    changed_by character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    created_by character varying(255) NOT NULL,
    version bigint NOT NULL,
    netex_id character varying(255) NOT NULL,
    description character varying(255),
    name character varying(255),
    private_code character varying(255),
    short_name character varying(255),
    direction_type character varying(255),
    provider_pk bigint NOT NULL,
    flexible_line_pk bigint NOT NULL
);


ALTER TABLE journey_pattern OWNER TO nabu;

--
-- TOC entry 241 (class 1259 OID 7889159)
-- Name: journey_pattern_notices; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE journey_pattern_notices (
    journey_pattern_pk bigint NOT NULL,
    notices_pk bigint NOT NULL
);


ALTER TABLE journey_pattern_notices OWNER TO nabu;

--
-- TOC entry 213 (class 1259 OID 7889019)
-- Name: journey_pattern_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE journey_pattern_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE journey_pattern_seq OWNER TO nabu;

--
-- TOC entry 242 (class 1259 OID 7889162)
-- Name: network; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE network (
    pk bigint NOT NULL,
    changed timestamp without time zone NOT NULL,
    changed_by character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    created_by character varying(255) NOT NULL,
    version bigint NOT NULL,
    netex_id character varying(255) NOT NULL,
    description character varying(255),
    name character varying(255),
    private_code character varying(255),
    short_name character varying(255),
    authority_ref character varying(255) NOT NULL,
    provider_pk bigint NOT NULL
);


ALTER TABLE network OWNER TO nabu;

--
-- TOC entry 214 (class 1259 OID 7889021)
-- Name: network_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE network_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE network_seq OWNER TO nabu;

--
-- TOC entry 243 (class 1259 OID 7889170)
-- Name: notice; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE notice (
    pk bigint NOT NULL,
    changed timestamp without time zone NOT NULL,
    changed_by character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    created_by character varying(255) NOT NULL,
    version bigint NOT NULL,
    netex_id character varying(255) NOT NULL,
    text character varying(255) NOT NULL,
    provider_pk bigint NOT NULL
);


ALTER TABLE notice OWNER TO nabu;

--
-- TOC entry 215 (class 1259 OID 7889023)
-- Name: notice_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE notice_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE notice_seq OWNER TO nabu;

--
-- TOC entry 244 (class 1259 OID 7889178)
-- Name: operating_period; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE operating_period (
    pk bigint NOT NULL,
    changed timestamp without time zone NOT NULL,
    changed_by character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    created_by character varying(255) NOT NULL,
    version bigint NOT NULL,
    from_date timestamp without time zone NOT NULL,
    to_date timestamp without time zone NOT NULL
);


ALTER TABLE operating_period OWNER TO nabu;

--
-- TOC entry 216 (class 1259 OID 7889025)
-- Name: operating_period_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE operating_period_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE operating_period_seq OWNER TO nabu;

--
-- TOC entry 245 (class 1259 OID 7889186)
-- Name: persistable_polygon; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE persistable_polygon (
    id bigint NOT NULL,
    polygon geometry NOT NULL
);


ALTER TABLE persistable_polygon OWNER TO nabu;

--
-- TOC entry 217 (class 1259 OID 7889027)
-- Name: persistable_polygon_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE persistable_polygon_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE persistable_polygon_seq OWNER TO nabu;

--
-- TOC entry 246 (class 1259 OID 7889194)
-- Name: provider; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE provider (
    pk bigint NOT NULL,
    changed timestamp without time zone NOT NULL,
    changed_by character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    created_by character varying(255) NOT NULL,
    version bigint NOT NULL,
    code character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    codespace_pk bigint NOT NULL
);


ALTER TABLE provider OWNER TO nabu;

--
-- TOC entry 218 (class 1259 OID 7889029)
-- Name: provider_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE provider_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE provider_seq OWNER TO nabu;

--
-- TOC entry 247 (class 1259 OID 7889202)
-- Name: service_journey; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE service_journey (
    pk bigint NOT NULL,
    changed timestamp without time zone NOT NULL,
    changed_by character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    created_by character varying(255) NOT NULL,
    version bigint NOT NULL,
    netex_id character varying(255) NOT NULL,
    description character varying(255),
    name character varying(255),
    private_code character varying(255),
    short_name character varying(255),
    operator_ref character varying(255),
    public_code character varying(255),
    provider_pk bigint NOT NULL,
    booking_arrangement_pk bigint,
    journey_pattern_pk bigint NOT NULL
);


ALTER TABLE service_journey OWNER TO nabu;

--
-- TOC entry 248 (class 1259 OID 7889210)
-- Name: service_journey_day_types; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE service_journey_day_types (
    service_journey_pk bigint NOT NULL,
    day_types_pk bigint NOT NULL
);


ALTER TABLE service_journey_day_types OWNER TO nabu;

--
-- TOC entry 249 (class 1259 OID 7889213)
-- Name: service_journey_notices; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE service_journey_notices (
    service_journey_pk bigint NOT NULL,
    notices_pk bigint NOT NULL
);


ALTER TABLE service_journey_notices OWNER TO nabu;

--
-- TOC entry 219 (class 1259 OID 7889031)
-- Name: service_journey_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE service_journey_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE service_journey_seq OWNER TO nabu;

--
-- TOC entry 250 (class 1259 OID 7889216)
-- Name: stop_point_in_journey_pattern; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE stop_point_in_journey_pattern (
    pk bigint NOT NULL,
    changed timestamp without time zone NOT NULL,
    changed_by character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    created_by character varying(255) NOT NULL,
    version bigint NOT NULL,
    netex_id character varying(255) NOT NULL,
    for_alighting boolean,
    for_boarding boolean,
    order_val integer,
    quay_ref character varying(255),
    provider_pk bigint NOT NULL,
    booking_arrangement_pk bigint,
    destination_display_pk bigint,
    flexible_stop_place_pk bigint,
    journey_pattern_pk bigint NOT NULL,
    CONSTRAINT stop_point_in_journey_pattern_order_val_check CHECK ((order_val >= 1))
);


ALTER TABLE stop_point_in_journey_pattern OWNER TO nabu;

--
-- TOC entry 251 (class 1259 OID 7889225)
-- Name: stop_point_in_journey_pattern_notices; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE stop_point_in_journey_pattern_notices (
    stop_point_in_journey_pattern_pk bigint NOT NULL,
    notices_pk bigint NOT NULL
);


ALTER TABLE stop_point_in_journey_pattern_notices OWNER TO nabu;

--
-- TOC entry 220 (class 1259 OID 7889033)
-- Name: stop_point_in_journey_pattern_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE stop_point_in_journey_pattern_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE stop_point_in_journey_pattern_seq OWNER TO nabu;

--
-- TOC entry 252 (class 1259 OID 7889228)
-- Name: timetabled_passing_time; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE timetabled_passing_time (
    pk bigint NOT NULL,
    changed timestamp without time zone NOT NULL,
    changed_by character varying(255) NOT NULL,
    created timestamp without time zone NOT NULL,
    created_by character varying(255) NOT NULL,
    version bigint NOT NULL,
    netex_id character varying(255) NOT NULL,
    arrival_day_offset integer NOT NULL,
    arrival_time timestamp without time zone,
    departure_day_offset integer NOT NULL,
    departure_time timestamp without time zone,
    earliest_departure_day_offset integer NOT NULL,
    earliest_departure_time timestamp without time zone,
    latest_arrival_day_offset integer NOT NULL,
    latest_arrival_time timestamp without time zone,
    order_val integer,
    provider_pk bigint NOT NULL,
    service_journey_pk bigint NOT NULL,
    CONSTRAINT timetabled_passing_time_order_val_check CHECK ((order_val >= 1))
);


ALTER TABLE timetabled_passing_time OWNER TO nabu;

--
-- TOC entry 253 (class 1259 OID 7889237)
-- Name: timetabled_passing_time_notices; Type: TABLE; Schema: public; Owner: nabu
--

CREATE TABLE timetabled_passing_time_notices (
    timetabled_passing_time_pk bigint NOT NULL,
    notices_pk bigint NOT NULL
);


ALTER TABLE timetabled_passing_time_notices OWNER TO nabu;

--
-- TOC entry 221 (class 1259 OID 7889035)
-- Name: timetabled_passing_time_seq; Type: SEQUENCE; Schema: public; Owner: nabu
--

CREATE SEQUENCE timetabled_passing_time_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE timetabled_passing_time_seq OWNER TO nabu;

--
-- TOC entry 3816 (class 0 OID 7889037)
-- Dependencies: 222
-- Data for Name: booking_arrangement; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY booking_arrangement (pk, changed, changed_by, created, created_by, version, book_when, booking_access, booking_note, latest_booking_time, minimum_booking_period, booking_contact_pk) FROM stdin;
\.


--
-- TOC entry 3817 (class 0 OID 7889045)
-- Dependencies: 223
-- Data for Name: booking_arrangement_booking_methods; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY booking_arrangement_booking_methods (booking_arrangement_pk, booking_methods) FROM stdin;
\.


--
-- TOC entry 3818 (class 0 OID 7889048)
-- Dependencies: 224
-- Data for Name: booking_arrangement_buy_when; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY booking_arrangement_buy_when (booking_arrangement_pk, buy_when) FROM stdin;
\.


--
-- TOC entry 3857 (class 0 OID 0)
-- Dependencies: 201
-- Name: booking_arrangement_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('booking_arrangement_seq', 1, false);


--
-- TOC entry 3858 (class 0 OID 0)
-- Dependencies: 200
-- Name: code_space_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('code_space_seq', 21, true);


--
-- TOC entry 3819 (class 0 OID 7889051)
-- Dependencies: 225
-- Data for Name: codespace; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY codespace (pk, changed, changed_by, created, created_by, version, xmlns, xmlns_url) FROM stdin;
1	2018-10-26 14:39:35.281533	test	2018-10-26 14:39:35.281533	test	1	NSB	http://www.rutebanken.org/ns/nsb
11	2018-10-26 14:39:35.290052	test	2018-10-26 14:39:35.290052	test	1	RUT	http://www.rutebanken.org/ns/rut
\.


--
-- TOC entry 3859 (class 0 OID 0)
-- Dependencies: 202
-- Name: codespace_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('codespace_seq', 11, true);


--
-- TOC entry 3820 (class 0 OID 7889059)
-- Dependencies: 226
-- Data for Name: contact; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY contact (pk, changed, changed_by, created, created_by, version, contact_person, email, further_details, phone, url) FROM stdin;
\.


--
-- TOC entry 3860 (class 0 OID 0)
-- Dependencies: 203
-- Name: contact_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('contact_seq', 1, false);


--
-- TOC entry 3821 (class 0 OID 7889067)
-- Dependencies: 227
-- Data for Name: day_type; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY day_type (pk, changed, changed_by, created, created_by, version, netex_id, provider_pk) FROM stdin;
1	2018-10-26 14:58:03.619	anonymousUser	2018-10-26 14:58:03.619	anonymousUser	0	RUT:DayType:c3f00a82-aa53-448c-aa20-795e079c725a	11
\.


--
-- TOC entry 3824 (class 0 OID 7889081)
-- Dependencies: 230
-- Data for Name: day_type_assignment; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY day_type_assignment (pk, changed, changed_by, created, created_by, version, available, date, operating_period_pk) FROM stdin;
1	2018-10-26 14:58:03.622	anonymousUser	2018-10-26 14:58:03.622	anonymousUser	0	\N	2018-10-21 00:00:00	\N
2	2018-10-26 14:58:03.624	anonymousUser	2018-10-26 14:58:03.624	anonymousUser	0	\N	\N	1
3	2018-10-26 14:58:03.627	anonymousUser	2018-10-26 14:58:03.627	anonymousUser	0	f	2018-10-19 00:00:00	\N
\.


--
-- TOC entry 3861 (class 0 OID 0)
-- Dependencies: 205
-- Name: day_type_assignment_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('day_type_assignment_seq', 11, true);


--
-- TOC entry 3823 (class 0 OID 7889078)
-- Dependencies: 229
-- Data for Name: day_type_day_type_assignments; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY day_type_day_type_assignments (day_type_pk, day_type_assignments_pk) FROM stdin;
1	1
1	2
1	3
\.


--
-- TOC entry 3822 (class 0 OID 7889075)
-- Dependencies: 228
-- Data for Name: day_type_days_of_week; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY day_type_days_of_week (day_type_pk, days_of_week) FROM stdin;
1	MONDAY
\.


--
-- TOC entry 3793 (class 0 OID 7833487)
-- Dependencies: 199
-- Data for Name: day_type_operating_periods; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY day_type_operating_periods (day_type_pk, operating_periods_pk) FROM stdin;
\.


--
-- TOC entry 3862 (class 0 OID 0)
-- Dependencies: 204
-- Name: day_type_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('day_type_seq', 11, true);


--
-- TOC entry 3825 (class 0 OID 7889089)
-- Dependencies: 231
-- Data for Name: destination_display; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY destination_display (pk, changed, changed_by, created, created_by, version, netex_id, front_text, provider_pk) FROM stdin;
1	2018-10-26 14:58:03.612	anonymousUser	2018-10-26 14:58:03.612	anonymousUser	0	RUT:DestinationDisplay:db0f9ec6-b62f-47ce-9417-ea1349cf1c48	krokkus	11
\.


--
-- TOC entry 3863 (class 0 OID 0)
-- Dependencies: 206
-- Name: destination_display_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('destination_display_seq', 11, true);


--
-- TOC entry 3826 (class 0 OID 7889097)
-- Dependencies: 232
-- Data for Name: export; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY export (pk, changed, changed_by, created, created_by, version, netex_id, export_status, from_date, name, to_date, provider_pk) FROM stdin;
\.


--
-- TOC entry 3828 (class 0 OID 7889108)
-- Dependencies: 234
-- Data for Name: export_message; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY export_message (pk, message, severity) FROM stdin;
\.


--
-- TOC entry 3864 (class 0 OID 0)
-- Dependencies: 208
-- Name: export_message_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('export_message_seq', 1, false);


--
-- TOC entry 3827 (class 0 OID 7889105)
-- Dependencies: 233
-- Data for Name: export_messages; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY export_messages (export_pk, messages_pk) FROM stdin;
\.


--
-- TOC entry 3865 (class 0 OID 0)
-- Dependencies: 207
-- Name: export_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('export_seq', 1, false);


--
-- TOC entry 3829 (class 0 OID 7889116)
-- Dependencies: 235
-- Data for Name: flexible_area; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY flexible_area (pk, changed, changed_by, created, created_by, version, polygon_id) FROM stdin;
1	2018-10-26 14:56:51.334	anonymousUser	2018-10-26 14:56:51.334	anonymousUser	0	1
\.


--
-- TOC entry 3866 (class 0 OID 0)
-- Dependencies: 209
-- Name: flexible_area_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('flexible_area_seq', 11, true);


--
-- TOC entry 3830 (class 0 OID 7889124)
-- Dependencies: 236
-- Data for Name: flexible_line; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY flexible_line (pk, changed, changed_by, created, created_by, version, netex_id, description, name, private_code, short_name, flexible_line_type, operator_ref, public_code, transport_mode, transport_submode, provider_pk, booking_arrangement_pk, network_pk) FROM stdin;
1	2018-10-26 14:58:03.6	anonymousUser	2018-10-26 14:58:03.6	anonymousUser	0	RUT:FlexibleLine:0be26dc3-0e68-4b97-960d-6a170507711d	\N	Test42g	\N	\N	FLEXIBLE_AREAS_ONLY	22	pubCode	BUS	LOCAL_BUS	11	\N	1
\.


--
-- TOC entry 3831 (class 0 OID 7889132)
-- Dependencies: 237
-- Data for Name: flexible_line_notices; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY flexible_line_notices (flexible_line_pk, notices_pk) FROM stdin;
\.


--
-- TOC entry 3867 (class 0 OID 0)
-- Dependencies: 210
-- Name: flexible_line_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('flexible_line_seq', 11, true);


--
-- TOC entry 3832 (class 0 OID 7889135)
-- Dependencies: 238
-- Data for Name: flexible_stop_place; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY flexible_stop_place (pk, changed, changed_by, created, created_by, version, netex_id, description, name, private_code, short_name, transport_mode, provider_pk, flexible_area_pk, hail_and_ride_area_pk) FROM stdin;
1	2018-10-26 14:56:51.331	anonymousUser	2018-10-26 14:56:51.331	anonymousUser	0	RUT:FlexibleStopPlace:a025308e-3851-4620-a4f1-53fad2727154	\N	Test	\N	\N	BUS	11	1	\N
\.


--
-- TOC entry 3868 (class 0 OID 0)
-- Dependencies: 211
-- Name: flexible_stop_place_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('flexible_stop_place_seq', 11, true);


--
-- TOC entry 3833 (class 0 OID 7889143)
-- Dependencies: 239
-- Data for Name: hail_and_ride_area; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY hail_and_ride_area (pk, changed, changed_by, created, created_by, version, end_quay_ref, start_quay_ref) FROM stdin;
\.


--
-- TOC entry 3869 (class 0 OID 0)
-- Dependencies: 212
-- Name: hail_and_ride_area_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('hail_and_ride_area_seq', 1, false);


--
-- TOC entry 3792 (class 0 OID 7802918)
-- Dependencies: 198
-- Data for Name: job; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY job (pk, changed, changed_by, created, created_by, version, netex_id, completed, provider_pk) FROM stdin;
\.


--
-- TOC entry 3870 (class 0 OID 0)
-- Dependencies: 197
-- Name: job_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('job_seq', 1, false);


--
-- TOC entry 3834 (class 0 OID 7889151)
-- Dependencies: 240
-- Data for Name: journey_pattern; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY journey_pattern (pk, changed, changed_by, created, created_by, version, netex_id, description, name, private_code, short_name, direction_type, provider_pk, flexible_line_pk) FROM stdin;
1	2018-10-26 14:58:03.603	anonymousUser	2018-10-26 14:58:03.603	anonymousUser	0	RUT:JourneyPattern:8b9d99ad-1d33-4c01-9192-780357bdc42d	\N	\N	\N	\N	\N	11	1
\.


--
-- TOC entry 3835 (class 0 OID 7889159)
-- Dependencies: 241
-- Data for Name: journey_pattern_notices; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY journey_pattern_notices (journey_pattern_pk, notices_pk) FROM stdin;
\.


--
-- TOC entry 3871 (class 0 OID 0)
-- Dependencies: 213
-- Name: journey_pattern_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('journey_pattern_seq', 11, true);


--
-- TOC entry 3836 (class 0 OID 7889162)
-- Dependencies: 242
-- Data for Name: network; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY network (pk, changed, changed_by, created, created_by, version, netex_id, description, name, private_code, short_name, authority_ref, provider_pk) FROM stdin;
1	2018-10-26 14:56:46.498	anonymousUser	2018-10-26 14:56:46.498	anonymousUser	0	RUT:Network:8c24b752-24eb-42db-a243-0ed6afdd6292	\N	TestNetwork	\N	\N	20	11
\.


--
-- TOC entry 3872 (class 0 OID 0)
-- Dependencies: 214
-- Name: network_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('network_seq', 11, true);


--
-- TOC entry 3837 (class 0 OID 7889170)
-- Dependencies: 243
-- Data for Name: notice; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY notice (pk, changed, changed_by, created, created_by, version, netex_id, text, provider_pk) FROM stdin;
\.


--
-- TOC entry 3873 (class 0 OID 0)
-- Dependencies: 215
-- Name: notice_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('notice_seq', 1, false);


--
-- TOC entry 3838 (class 0 OID 7889178)
-- Dependencies: 244
-- Data for Name: operating_period; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY operating_period (pk, changed, changed_by, created, created_by, version, from_date, to_date) FROM stdin;
1	2018-10-26 14:58:03.624	anonymousUser	2018-10-26 14:58:03.624	anonymousUser	0	2018-02-01 00:00:00	2018-03-12 00:00:00
\.


--
-- TOC entry 3874 (class 0 OID 0)
-- Dependencies: 216
-- Name: operating_period_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('operating_period_seq', 11, true);


--
-- TOC entry 3839 (class 0 OID 7889186)
-- Dependencies: 245
-- Data for Name: persistable_polygon; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY persistable_polygon (id, polygon) FROM stdin;
1	0103000020E61000000100000004000000CDCCCCCCCCCC00406666666666660A406666666666661040CDCCCCCCCCCC14409A999999999913409A99999999991740CDCCCCCCCCCC00406666666666660A40
\.


--
-- TOC entry 3875 (class 0 OID 0)
-- Dependencies: 217
-- Name: persistable_polygon_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('persistable_polygon_seq', 11, true);


--
-- TOC entry 3840 (class 0 OID 7889194)
-- Dependencies: 246
-- Data for Name: provider; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY provider (pk, changed, changed_by, created, created_by, version, code, name, codespace_pk) FROM stdin;
1	2018-10-26 14:39:35.29723	test	2018-10-26 14:39:35.29723	test	1	nsb	NSB	1
11	2018-10-26 14:39:35.306524	test	2018-10-26 14:39:35.306524	test	1	rut	Ruter	11
\.


--
-- TOC entry 3876 (class 0 OID 0)
-- Dependencies: 218
-- Name: provider_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('provider_seq', 11, true);


--
-- TOC entry 3841 (class 0 OID 7889202)
-- Dependencies: 247
-- Data for Name: service_journey; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY service_journey (pk, changed, changed_by, created, created_by, version, netex_id, description, name, private_code, short_name, operator_ref, public_code, provider_pk, booking_arrangement_pk, journey_pattern_pk) FROM stdin;
1	2018-10-26 14:58:03.616	anonymousUser	2018-10-26 14:58:03.616	anonymousUser	0	RUT:ServiceJourney:27f1a646-e2f2-4f51-a4df-271da96ab0e0	\N	\N	\N	\N	\N	\N	11	\N	1
\.


--
-- TOC entry 3842 (class 0 OID 7889210)
-- Dependencies: 248
-- Data for Name: service_journey_day_types; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY service_journey_day_types (service_journey_pk, day_types_pk) FROM stdin;
1	1
\.


--
-- TOC entry 3843 (class 0 OID 7889213)
-- Dependencies: 249
-- Data for Name: service_journey_notices; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY service_journey_notices (service_journey_pk, notices_pk) FROM stdin;
\.


--
-- TOC entry 3877 (class 0 OID 0)
-- Dependencies: 219
-- Name: service_journey_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('service_journey_seq', 11, true);


--
-- TOC entry 3527 (class 0 OID 7795748)
-- Dependencies: 183
-- Data for Name: spatial_ref_sys; Type: TABLE DATA; Schema: public; Owner: chouette
--

COPY spatial_ref_sys  FROM stdin;
\.


--
-- TOC entry 3844 (class 0 OID 7889216)
-- Dependencies: 250
-- Data for Name: stop_point_in_journey_pattern; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY stop_point_in_journey_pattern (pk, changed, changed_by, created, created_by, version, netex_id, for_alighting, for_boarding, order_val, quay_ref, provider_pk, booking_arrangement_pk, destination_display_pk, flexible_stop_place_pk, journey_pattern_pk) FROM stdin;
1	2018-10-26 14:58:03.608	anonymousUser	2018-10-26 14:58:03.608	anonymousUser	0	RUT:StopPointInJourneyPattern:b6724d57-c84e-4ad0-8143-91a22ca0baf6	\N	\N	1	\N	11	\N	1	1	1
2	2018-10-26 14:58:03.615	anonymousUser	2018-10-26 14:58:03.615	anonymousUser	0	RUT:StopPointInJourneyPattern:8c0c3dd5-2b67-4dcb-9160-b88f285e24b2	\N	\N	2	\N	11	\N	\N	1	1
\.


--
-- TOC entry 3845 (class 0 OID 7889225)
-- Dependencies: 251
-- Data for Name: stop_point_in_journey_pattern_notices; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY stop_point_in_journey_pattern_notices (stop_point_in_journey_pattern_pk, notices_pk) FROM stdin;
\.


--
-- TOC entry 3878 (class 0 OID 0)
-- Dependencies: 220
-- Name: stop_point_in_journey_pattern_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('stop_point_in_journey_pattern_seq', 11, true);


--
-- TOC entry 3846 (class 0 OID 7889228)
-- Dependencies: 252
-- Data for Name: timetabled_passing_time; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY timetabled_passing_time (pk, changed, changed_by, created, created_by, version, netex_id, arrival_day_offset, arrival_time, departure_day_offset, departure_time, earliest_departure_day_offset, earliest_departure_time, latest_arrival_day_offset, latest_arrival_time, order_val, provider_pk, service_journey_pk) FROM stdin;
1	2018-10-26 14:58:03.628	anonymousUser	2018-10-26 14:58:03.628	anonymousUser	0	RUT:TimetabledPassingTime:b332a1a7-5617-4d50-ade9-3e46163b0300	0	\N	0	\N	0	2018-10-26 10:00:00	0	\N	1	11	1
2	2018-10-26 14:58:03.642	anonymousUser	2018-10-26 14:58:03.642	anonymousUser	0	RUT:TimetabledPassingTime:bdba1725-5121-4c57-8373-1463842181f8	0	\N	0	\N	0	\N	0	2018-10-26 16:00:00	2	11	1
\.


--
-- TOC entry 3847 (class 0 OID 7889237)
-- Dependencies: 253
-- Data for Name: timetabled_passing_time_notices; Type: TABLE DATA; Schema: public; Owner: nabu
--

COPY timetabled_passing_time_notices (timetabled_passing_time_pk, notices_pk) FROM stdin;
\.


--
-- TOC entry 3879 (class 0 OID 0)
-- Dependencies: 221
-- Name: timetabled_passing_time_seq; Type: SEQUENCE SET; Schema: public; Owner: nabu
--

SELECT pg_catalog.setval('timetabled_passing_time_seq', 11, true);


--
-- TOC entry 3537 (class 2606 OID 7889044)
-- Name: booking_arrangement_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY booking_arrangement
    ADD CONSTRAINT booking_arrangement_pkey PRIMARY KEY (pk);


--
-- TOC entry 3539 (class 2606 OID 7889058)
-- Name: codespace_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY codespace
    ADD CONSTRAINT codespace_pkey PRIMARY KEY (pk);


--
-- TOC entry 3541 (class 2606 OID 7889241)
-- Name: codespace_unique_xmlns_constraint; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY codespace
    ADD CONSTRAINT codespace_unique_xmlns_constraint UNIQUE (xmlns);


--
-- TOC entry 3543 (class 2606 OID 7889066)
-- Name: contact_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY contact
    ADD CONSTRAINT contact_pkey PRIMARY KEY (pk);


--
-- TOC entry 3551 (class 2606 OID 7889088)
-- Name: day_type_assignment_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY day_type_assignment
    ADD CONSTRAINT day_type_assignment_pkey PRIMARY KEY (pk);


--
-- TOC entry 3545 (class 2606 OID 7889074)
-- Name: day_type_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY day_type
    ADD CONSTRAINT day_type_pkey PRIMARY KEY (pk);


--
-- TOC entry 3553 (class 2606 OID 7889096)
-- Name: destination_display_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY destination_display
    ADD CONSTRAINT destination_display_pkey PRIMARY KEY (pk);


--
-- TOC entry 3563 (class 2606 OID 7889115)
-- Name: export_message_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY export_message
    ADD CONSTRAINT export_message_pkey PRIMARY KEY (pk);


--
-- TOC entry 3557 (class 2606 OID 7889104)
-- Name: export_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY export
    ADD CONSTRAINT export_pkey PRIMARY KEY (pk);


--
-- TOC entry 3565 (class 2606 OID 7889123)
-- Name: flexible_area_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY flexible_area
    ADD CONSTRAINT flexible_area_pkey PRIMARY KEY (pk);


--
-- TOC entry 3567 (class 2606 OID 7889131)
-- Name: flexible_line_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY flexible_line
    ADD CONSTRAINT flexible_line_pkey PRIMARY KEY (pk);


--
-- TOC entry 3569 (class 2606 OID 7889253)
-- Name: flexible_line_unique_name_constrain; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY flexible_line
    ADD CONSTRAINT flexible_line_unique_name_constrain UNIQUE (provider_pk, name);


--
-- TOC entry 3573 (class 2606 OID 7889142)
-- Name: flexible_stop_place_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY flexible_stop_place
    ADD CONSTRAINT flexible_stop_place_pkey PRIMARY KEY (pk);


--
-- TOC entry 3575 (class 2606 OID 7889257)
-- Name: flexible_stop_place_unique_name_constraint; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY flexible_stop_place
    ADD CONSTRAINT flexible_stop_place_unique_name_constraint UNIQUE (provider_pk, name);


--
-- TOC entry 3579 (class 2606 OID 7889150)
-- Name: hail_and_ride_area_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY hail_and_ride_area
    ADD CONSTRAINT hail_and_ride_area_pkey PRIMARY KEY (pk);


--
-- TOC entry 3531 (class 2606 OID 7802925)
-- Name: job_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY job
    ADD CONSTRAINT job_pkey PRIMARY KEY (pk);


--
-- TOC entry 3581 (class 2606 OID 7889158)
-- Name: journey_pattern_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY journey_pattern
    ADD CONSTRAINT journey_pattern_pkey PRIMARY KEY (pk);


--
-- TOC entry 3583 (class 2606 OID 7889261)
-- Name: journey_pattern_unique_name_constraint; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY journey_pattern
    ADD CONSTRAINT journey_pattern_unique_name_constraint UNIQUE (provider_pk, name);


--
-- TOC entry 3587 (class 2606 OID 7889169)
-- Name: network_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY network
    ADD CONSTRAINT network_pkey PRIMARY KEY (pk);


--
-- TOC entry 3589 (class 2606 OID 7889265)
-- Name: network_unique_name_constraint; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY network
    ADD CONSTRAINT network_unique_name_constraint UNIQUE (provider_pk, name);


--
-- TOC entry 3593 (class 2606 OID 7889177)
-- Name: notice_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY notice
    ADD CONSTRAINT notice_pkey PRIMARY KEY (pk);


--
-- TOC entry 3597 (class 2606 OID 7889185)
-- Name: operating_period_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY operating_period
    ADD CONSTRAINT operating_period_pkey PRIMARY KEY (pk);


--
-- TOC entry 3599 (class 2606 OID 7889193)
-- Name: persistable_polygon_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY persistable_polygon
    ADD CONSTRAINT persistable_polygon_pkey PRIMARY KEY (id);


--
-- TOC entry 3601 (class 2606 OID 7889201)
-- Name: provider_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY provider
    ADD CONSTRAINT provider_pkey PRIMARY KEY (pk);


--
-- TOC entry 3603 (class 2606 OID 7889271)
-- Name: provider_unique_code_constraint; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY provider
    ADD CONSTRAINT provider_unique_code_constraint UNIQUE (code);


--
-- TOC entry 3605 (class 2606 OID 7889209)
-- Name: service_journey_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY service_journey
    ADD CONSTRAINT service_journey_pkey PRIMARY KEY (pk);


--
-- TOC entry 3607 (class 2606 OID 7889273)
-- Name: service_journey_unique_name_constraint; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY service_journey
    ADD CONSTRAINT service_journey_unique_name_constraint UNIQUE (provider_pk, name);


--
-- TOC entry 3613 (class 2606 OID 7889224)
-- Name: stop_point_in_journey_pattern_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY stop_point_in_journey_pattern
    ADD CONSTRAINT stop_point_in_journey_pattern_pkey PRIMARY KEY (pk);


--
-- TOC entry 3615 (class 2606 OID 7889279)
-- Name: stop_point_in_jp_unique_order_constraint; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY stop_point_in_journey_pattern
    ADD CONSTRAINT stop_point_in_jp_unique_order_constraint UNIQUE (journey_pattern_pk, order_val);


--
-- TOC entry 3619 (class 2606 OID 7889236)
-- Name: timetabled_passing_time_pkey; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY timetabled_passing_time
    ADD CONSTRAINT timetabled_passing_time_pkey PRIMARY KEY (pk);


--
-- TOC entry 3621 (class 2606 OID 7889283)
-- Name: timetabled_passing_time_unique_order_constraint; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY timetabled_passing_time
    ADD CONSTRAINT timetabled_passing_time_unique_order_constraint UNIQUE (service_journey_pk, order_val);


--
-- TOC entry 3623 (class 2606 OID 7889285)
-- Name: uk_1rbnx4cuhllxb2x2oinipw32f; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY timetabled_passing_time
    ADD CONSTRAINT uk_1rbnx4cuhllxb2x2oinipw32f UNIQUE (netex_id);


--
-- TOC entry 3561 (class 2606 OID 7889251)
-- Name: uk_1wty5x50tf3i6csjrv266manj; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY export_messages
    ADD CONSTRAINT uk_1wty5x50tf3i6csjrv266manj UNIQUE (messages_pk);


--
-- TOC entry 3533 (class 2606 OID 7802955)
-- Name: uk_2tuyrfg2iljl233pen6pcg6nr; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY job
    ADD CONSTRAINT uk_2tuyrfg2iljl233pen6pcg6nr UNIQUE (netex_id);


--
-- TOC entry 3609 (class 2606 OID 7889275)
-- Name: uk_2yitv45vtmnp4y9cuxc42l8v1; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY service_journey
    ADD CONSTRAINT uk_2yitv45vtmnp4y9cuxc42l8v1 UNIQUE (netex_id);


--
-- TOC entry 3535 (class 2606 OID 7833568)
-- Name: uk_33n6fm4mfltindhbrpyoetrtj; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY day_type_operating_periods
    ADD CONSTRAINT uk_33n6fm4mfltindhbrpyoetrtj UNIQUE (operating_periods_pk);


--
-- TOC entry 3585 (class 2606 OID 7889263)
-- Name: uk_6opdwsv0wx70w0uhlmlsvu4ff; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY journey_pattern
    ADD CONSTRAINT uk_6opdwsv0wx70w0uhlmlsvu4ff UNIQUE (netex_id);


--
-- TOC entry 3577 (class 2606 OID 7889259)
-- Name: uk_9h7tij83xx8wwsvcqjospwdt5; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY flexible_stop_place
    ADD CONSTRAINT uk_9h7tij83xx8wwsvcqjospwdt5 UNIQUE (netex_id);


--
-- TOC entry 3559 (class 2606 OID 7889249)
-- Name: uk_dnwkw70ycyb4pmwaouqcnsbi9; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY export
    ADD CONSTRAINT uk_dnwkw70ycyb4pmwaouqcnsbi9 UNIQUE (netex_id);


--
-- TOC entry 3571 (class 2606 OID 7889255)
-- Name: uk_gtca4g1h1ab2i8bbwuhual917; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY flexible_line
    ADD CONSTRAINT uk_gtca4g1h1ab2i8bbwuhual917 UNIQUE (netex_id);


--
-- TOC entry 3617 (class 2606 OID 7889281)
-- Name: uk_im3ufgncscusou6em93kt1wa; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY stop_point_in_journey_pattern
    ADD CONSTRAINT uk_im3ufgncscusou6em93kt1wa UNIQUE (netex_id);


--
-- TOC entry 3555 (class 2606 OID 7889247)
-- Name: uk_on1up2jgfb1ukn1ktr9y7ncpf; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY destination_display
    ADD CONSTRAINT uk_on1up2jgfb1ukn1ktr9y7ncpf UNIQUE (netex_id);


--
-- TOC entry 3549 (class 2606 OID 7889245)
-- Name: uk_q6whnpyylfy153g6r0ex1ddnv; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY day_type_day_type_assignments
    ADD CONSTRAINT uk_q6whnpyylfy153g6r0ex1ddnv UNIQUE (day_type_assignments_pk);


--
-- TOC entry 3611 (class 2606 OID 7889277)
-- Name: uk_qdc46a69wd522j6oseb4fiv1a; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY service_journey_day_types
    ADD CONSTRAINT uk_qdc46a69wd522j6oseb4fiv1a UNIQUE (day_types_pk);


--
-- TOC entry 3595 (class 2606 OID 7889269)
-- Name: uk_qfytiej0sqc6l9c7y32wb4fta; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY notice
    ADD CONSTRAINT uk_qfytiej0sqc6l9c7y32wb4fta UNIQUE (netex_id);


--
-- TOC entry 3591 (class 2606 OID 7889267)
-- Name: uk_rd2v99cuetlivwfyvbf8hh4bf; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY network
    ADD CONSTRAINT uk_rd2v99cuetlivwfyvbf8hh4bf UNIQUE (netex_id);


--
-- TOC entry 3547 (class 2606 OID 7889243)
-- Name: uk_t45a71icytoqww6mocl8taed0; Type: CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY day_type
    ADD CONSTRAINT uk_t45a71icytoqww6mocl8taed0 UNIQUE (netex_id);


--
-- TOC entry 3631 (class 2606 OID 7889321)
-- Name: fk297xs4o3xcq0cr10nu25b58oj; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY day_type_assignment
    ADD CONSTRAINT fk297xs4o3xcq0cr10nu25b58oj FOREIGN KEY (operating_period_pk) REFERENCES operating_period(pk);


--
-- TOC entry 3638 (class 2606 OID 7889356)
-- Name: fk2gaxk68lj0mrdky6i36c5ki1w; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY flexible_line
    ADD CONSTRAINT fk2gaxk68lj0mrdky6i36c5ki1w FOREIGN KEY (booking_arrangement_pk) REFERENCES booking_arrangement(pk);


--
-- TOC entry 3624 (class 2606 OID 7889286)
-- Name: fk31bnjqyyo48at6gdolicyegmp; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY booking_arrangement
    ADD CONSTRAINT fk31bnjqyyo48at6gdolicyegmp FOREIGN KEY (booking_contact_pk) REFERENCES contact(pk);


--
-- TOC entry 3663 (class 2606 OID 7889481)
-- Name: fk3dnm8qswvkbhjsexvwbfioby3; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY stop_point_in_journey_pattern
    ADD CONSTRAINT fk3dnm8qswvkbhjsexvwbfioby3 FOREIGN KEY (journey_pattern_pk) REFERENCES journey_pattern(pk);


--
-- TOC entry 3628 (class 2606 OID 7889306)
-- Name: fk3fwrdpmo8pktnceg5gj95s80q; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY day_type_days_of_week
    ADD CONSTRAINT fk3fwrdpmo8pktnceg5gj95s80q FOREIGN KEY (day_type_pk) REFERENCES day_type(pk);


--
-- TOC entry 3652 (class 2606 OID 7889426)
-- Name: fk3if9terbqtsr9ufaumb0egiu2; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY service_journey
    ADD CONSTRAINT fk3if9terbqtsr9ufaumb0egiu2 FOREIGN KEY (provider_pk) REFERENCES provider(pk);


--
-- TOC entry 3662 (class 2606 OID 7889476)
-- Name: fk3oxo9975n7ja8al2acf4d0bp8; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY stop_point_in_journey_pattern
    ADD CONSTRAINT fk3oxo9975n7ja8al2acf4d0bp8 FOREIGN KEY (flexible_stop_place_pk) REFERENCES flexible_stop_place(pk);


--
-- TOC entry 3660 (class 2606 OID 7889466)
-- Name: fk4bqk879vs5vyi44c9nx6fsmls; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY stop_point_in_journey_pattern
    ADD CONSTRAINT fk4bqk879vs5vyi44c9nx6fsmls FOREIGN KEY (booking_arrangement_pk) REFERENCES booking_arrangement(pk);


--
-- TOC entry 3626 (class 2606 OID 7889296)
-- Name: fk4ybw05hkhbjkcnc7s3spgmcgy; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY booking_arrangement_buy_when
    ADD CONSTRAINT fk4ybw05hkhbjkcnc7s3spgmcgy FOREIGN KEY (booking_arrangement_pk) REFERENCES booking_arrangement(pk);


--
-- TOC entry 3633 (class 2606 OID 7889331)
-- Name: fk57jfb1rhyv0cx61p8i69r37d0; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY export
    ADD CONSTRAINT fk57jfb1rhyv0cx61p8i69r37d0 FOREIGN KEY (provider_pk) REFERENCES provider(pk);


--
-- TOC entry 3625 (class 2606 OID 7889291)
-- Name: fk5u4gcqgdyjb0sw11im7mdq5ha; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY booking_arrangement_booking_methods
    ADD CONSTRAINT fk5u4gcqgdyjb0sw11im7mdq5ha FOREIGN KEY (booking_arrangement_pk) REFERENCES booking_arrangement(pk);


--
-- TOC entry 3637 (class 2606 OID 7889351)
-- Name: fk6atpd9rm2ilv74y7twkoyid3s; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY flexible_line
    ADD CONSTRAINT fk6atpd9rm2ilv74y7twkoyid3s FOREIGN KEY (provider_pk) REFERENCES provider(pk);


--
-- TOC entry 3650 (class 2606 OID 7889416)
-- Name: fk6qef9cj0r0xnpyr4cju81noib; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY notice
    ADD CONSTRAINT fk6qef9cj0r0xnpyr4cju81noib FOREIGN KEY (provider_pk) REFERENCES provider(pk);


--
-- TOC entry 3651 (class 2606 OID 7889421)
-- Name: fk7i94slrt06x9tddd7eoqiqu4d; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY provider
    ADD CONSTRAINT fk7i94slrt06x9tddd7eoqiqu4d FOREIGN KEY (codespace_pk) REFERENCES codespace(pk);


--
-- TOC entry 3643 (class 2606 OID 7889381)
-- Name: fkaw7sl3jvkoqpgajnb4wprbl9u; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY flexible_stop_place
    ADD CONSTRAINT fkaw7sl3jvkoqpgajnb4wprbl9u FOREIGN KEY (flexible_area_pk) REFERENCES flexible_area(pk);


--
-- TOC entry 3642 (class 2606 OID 7889376)
-- Name: fkba28lrugxk2hn8u22vwynp07d; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY flexible_stop_place
    ADD CONSTRAINT fkba28lrugxk2hn8u22vwynp07d FOREIGN KEY (provider_pk) REFERENCES provider(pk);


--
-- TOC entry 3656 (class 2606 OID 7889446)
-- Name: fkbnxjaenohmqt2b2348252p2x; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY service_journey_day_types
    ADD CONSTRAINT fkbnxjaenohmqt2b2348252p2x FOREIGN KEY (service_journey_pk) REFERENCES service_journey(pk);


--
-- TOC entry 3636 (class 2606 OID 7889346)
-- Name: fkbwooc86vk2ljaq1q64gbr17o9; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY flexible_area
    ADD CONSTRAINT fkbwooc86vk2ljaq1q64gbr17o9 FOREIGN KEY (polygon_id) REFERENCES persistable_polygon(id);


--
-- TOC entry 3644 (class 2606 OID 7889386)
-- Name: fkcbi8qb4lb6w2xmg4y1m61vnd4; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY flexible_stop_place
    ADD CONSTRAINT fkcbi8qb4lb6w2xmg4y1m61vnd4 FOREIGN KEY (hail_and_ride_area_pk) REFERENCES hail_and_ride_area(pk);


--
-- TOC entry 3657 (class 2606 OID 7889451)
-- Name: fkd8oifwg07p6qr5vg2pghp7fv2; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY service_journey_notices
    ADD CONSTRAINT fkd8oifwg07p6qr5vg2pghp7fv2 FOREIGN KEY (notices_pk) REFERENCES notice(pk);


--
-- TOC entry 3665 (class 2606 OID 7889491)
-- Name: fkdssn8xtq0j46i45numda1w8un; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY stop_point_in_journey_pattern_notices
    ADD CONSTRAINT fkdssn8xtq0j46i45numda1w8un FOREIGN KEY (stop_point_in_journey_pattern_pk) REFERENCES stop_point_in_journey_pattern(pk);


--
-- TOC entry 3664 (class 2606 OID 7889486)
-- Name: fkeqi59bwb1yrs1t2a42ukxuj8a; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY stop_point_in_journey_pattern_notices
    ADD CONSTRAINT fkeqi59bwb1yrs1t2a42ukxuj8a FOREIGN KEY (notices_pk) REFERENCES notice(pk);


--
-- TOC entry 3668 (class 2606 OID 7889506)
-- Name: fkf2b5uf62o3bmhmn2ogjmxyilt; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY timetabled_passing_time_notices
    ADD CONSTRAINT fkf2b5uf62o3bmhmn2ogjmxyilt FOREIGN KEY (notices_pk) REFERENCES notice(pk);


--
-- TOC entry 3647 (class 2606 OID 7889401)
-- Name: fkg029hjh1ha61fm4lsoieb6j1q; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY journey_pattern_notices
    ADD CONSTRAINT fkg029hjh1ha61fm4lsoieb6j1q FOREIGN KEY (notices_pk) REFERENCES notice(pk);


--
-- TOC entry 3645 (class 2606 OID 7889391)
-- Name: fkhmuc7co5oih4c8mc9wc2eyqrl; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY journey_pattern
    ADD CONSTRAINT fkhmuc7co5oih4c8mc9wc2eyqrl FOREIGN KEY (provider_pk) REFERENCES provider(pk);


--
-- TOC entry 3655 (class 2606 OID 7889441)
-- Name: fkj7dvn18ovdlu9r5nl165ocsl8; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY service_journey_day_types
    ADD CONSTRAINT fkj7dvn18ovdlu9r5nl165ocsl8 FOREIGN KEY (day_types_pk) REFERENCES day_type(pk);


--
-- TOC entry 3667 (class 2606 OID 7889501)
-- Name: fkjvmocxrhmf59g54o9002i5o7i; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY timetabled_passing_time
    ADD CONSTRAINT fkjvmocxrhmf59g54o9002i5o7i FOREIGN KEY (service_journey_pk) REFERENCES service_journey(pk);


--
-- TOC entry 3669 (class 2606 OID 7889511)
-- Name: fkk0bs0vo50fsffwc7u9pdxhv76; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY timetabled_passing_time_notices
    ADD CONSTRAINT fkk0bs0vo50fsffwc7u9pdxhv76 FOREIGN KEY (timetabled_passing_time_pk) REFERENCES timetabled_passing_time(pk);


--
-- TOC entry 3649 (class 2606 OID 7889411)
-- Name: fkk5djht99hq0uu38t1um89ekid; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY network
    ADD CONSTRAINT fkk5djht99hq0uu38t1um89ekid FOREIGN KEY (provider_pk) REFERENCES provider(pk);


--
-- TOC entry 3646 (class 2606 OID 7889396)
-- Name: fkkdks6fliky0ap99th0xk0m0np; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY journey_pattern
    ADD CONSTRAINT fkkdks6fliky0ap99th0xk0m0np FOREIGN KEY (flexible_line_pk) REFERENCES flexible_line(pk);


--
-- TOC entry 3639 (class 2606 OID 7889361)
-- Name: fkl5adp15hvgbhk9q05d2j0kixi; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY flexible_line
    ADD CONSTRAINT fkl5adp15hvgbhk9q05d2j0kixi FOREIGN KEY (network_pk) REFERENCES network(pk);


--
-- TOC entry 3648 (class 2606 OID 7889406)
-- Name: fknt3qnq4i3fv81jkyj8it8xp8g; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY journey_pattern_notices
    ADD CONSTRAINT fknt3qnq4i3fv81jkyj8it8xp8g FOREIGN KEY (journey_pattern_pk) REFERENCES journey_pattern(pk);


--
-- TOC entry 3653 (class 2606 OID 7889431)
-- Name: fknx8uhova7u4hq7b3afb0mijnx; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY service_journey
    ADD CONSTRAINT fknx8uhova7u4hq7b3afb0mijnx FOREIGN KEY (booking_arrangement_pk) REFERENCES booking_arrangement(pk);


--
-- TOC entry 3630 (class 2606 OID 7889316)
-- Name: fkod92pd3miuxb1axercypc8v6p; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY day_type_day_type_assignments
    ADD CONSTRAINT fkod92pd3miuxb1axercypc8v6p FOREIGN KEY (day_type_pk) REFERENCES day_type(pk);


--
-- TOC entry 3635 (class 2606 OID 7889341)
-- Name: fkot8h5rjwfv8b83dbtqcwexesi; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY export_messages
    ADD CONSTRAINT fkot8h5rjwfv8b83dbtqcwexesi FOREIGN KEY (export_pk) REFERENCES export(pk);


--
-- TOC entry 3659 (class 2606 OID 7889461)
-- Name: fkph9isere41j7cbw0olgrek5r1; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY stop_point_in_journey_pattern
    ADD CONSTRAINT fkph9isere41j7cbw0olgrek5r1 FOREIGN KEY (provider_pk) REFERENCES provider(pk);


--
-- TOC entry 3627 (class 2606 OID 7889301)
-- Name: fkpjhkku6buueark1fgiayndvwh; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY day_type
    ADD CONSTRAINT fkpjhkku6buueark1fgiayndvwh FOREIGN KEY (provider_pk) REFERENCES provider(pk);


--
-- TOC entry 3629 (class 2606 OID 7889311)
-- Name: fkpl0gjs3tdomyc4nb3urnwfjac; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY day_type_day_type_assignments
    ADD CONSTRAINT fkpl0gjs3tdomyc4nb3urnwfjac FOREIGN KEY (day_type_assignments_pk) REFERENCES day_type_assignment(pk);


--
-- TOC entry 3654 (class 2606 OID 7889436)
-- Name: fkqhd87e7dj6gvr05ub2j359a2b; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY service_journey
    ADD CONSTRAINT fkqhd87e7dj6gvr05ub2j359a2b FOREIGN KEY (journey_pattern_pk) REFERENCES journey_pattern(pk);


--
-- TOC entry 3632 (class 2606 OID 7889326)
-- Name: fkqpn6sk7vs2j1v3jnfsjyb84c5; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY destination_display
    ADD CONSTRAINT fkqpn6sk7vs2j1v3jnfsjyb84c5 FOREIGN KEY (provider_pk) REFERENCES provider(pk);


--
-- TOC entry 3658 (class 2606 OID 7889456)
-- Name: fkqwwl6n247pier4hajujlutavq; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY service_journey_notices
    ADD CONSTRAINT fkqwwl6n247pier4hajujlutavq FOREIGN KEY (service_journey_pk) REFERENCES service_journey(pk);


--
-- TOC entry 3640 (class 2606 OID 7889366)
-- Name: fkrnqapn9q1b1rdgg6a2xtbg8cd; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY flexible_line_notices
    ADD CONSTRAINT fkrnqapn9q1b1rdgg6a2xtbg8cd FOREIGN KEY (notices_pk) REFERENCES notice(pk);


--
-- TOC entry 3641 (class 2606 OID 7889371)
-- Name: fks1jhrwcc1y98cxstfat3g5vh9; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY flexible_line_notices
    ADD CONSTRAINT fks1jhrwcc1y98cxstfat3g5vh9 FOREIGN KEY (flexible_line_pk) REFERENCES flexible_line(pk);


--
-- TOC entry 3666 (class 2606 OID 7889496)
-- Name: fks597qdxeq8p2mumaitp7f1okd; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY timetabled_passing_time
    ADD CONSTRAINT fks597qdxeq8p2mumaitp7f1okd FOREIGN KEY (provider_pk) REFERENCES provider(pk);


--
-- TOC entry 3634 (class 2606 OID 7889336)
-- Name: fksjp6e602wysdiavsencuo5o1a; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY export_messages
    ADD CONSTRAINT fksjp6e602wysdiavsencuo5o1a FOREIGN KEY (messages_pk) REFERENCES export_message(pk);


--
-- TOC entry 3661 (class 2606 OID 7889471)
-- Name: fkw0j125pp28uepff1gkv8f0ji; Type: FK CONSTRAINT; Schema: public; Owner: nabu
--

ALTER TABLE ONLY stop_point_in_journey_pattern
    ADD CONSTRAINT fkw0j125pp28uepff1gkv8f0ji FOREIGN KEY (destination_display_pk) REFERENCES destination_display(pk);


--
-- TOC entry 3854 (class 0 OID 0)
-- Dependencies: 7
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2018-10-30 13:41:53 CET

--
-- PostgreSQL database dump complete
--