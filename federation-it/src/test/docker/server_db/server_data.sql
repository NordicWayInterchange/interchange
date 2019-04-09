--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.12
-- Dumped by pg_dump version 9.6.12

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner:
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner:
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: dat_seq; Type: SEQUENCE; Schema: public; Owner: federation
--

CREATE SEQUENCE public.dat_seq
  START WITH 1
  INCREMENT BY 50
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;


ALTER TABLE public.dat_seq OWNER TO federation;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: data_types; Type: TABLE; Schema: public; Owner: federation
--

CREATE TABLE public.data_types (
  dat_id integer NOT NULL,
  how character varying(255),
  last_updated timestamp without time zone,
  what character varying(255),
  where1 character varying(255),
  spr_id_cap integer,
  ixn_id_cap integer
);


ALTER TABLE public.data_types OWNER TO federation;

--
-- Name: interchanges; Type: TABLE; Schema: public; Owner: federation
--

CREATE TABLE public.interchanges (
  ixn_id integer NOT NULL,
  backoff_attempts integer NOT NULL,
  backoff_start timestamp without time zone,
  control_channel_port character varying(255),
  domain_name character varying(255),
  interchange_status character varying(255),
  last_seen timestamp without time zone,
  last_updated timestamp without time zone,
  message_channel_port character varying(255),
  name character varying(255)
);


ALTER TABLE public.interchanges OWNER TO federation;

--
-- Name: ixn_seq; Type: SEQUENCE; Schema: public; Owner: federation
--

CREATE SEQUENCE public.ixn_seq
  START WITH 1
  INCREMENT BY 50
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;


ALTER TABLE public.ixn_seq OWNER TO federation;

--
-- Name: service_providers; Type: TABLE; Schema: public; Owner: federation
--

CREATE TABLE public.service_providers (
  spr_id integer NOT NULL,
  name character varying(255)
);


ALTER TABLE public.service_providers OWNER TO federation;

--
-- Name: sp_generator; Type: SEQUENCE; Schema: public; Owner: federation
--

CREATE SEQUENCE public.sp_generator
  START WITH 1
  INCREMENT BY 50
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;


ALTER TABLE public.sp_generator OWNER TO federation;

--
-- Name: sub_seq; Type: SEQUENCE; Schema: public; Owner: federation
--

CREATE SEQUENCE public.sub_seq
  START WITH 1
  INCREMENT BY 50
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;


ALTER TABLE public.sub_seq OWNER TO federation;

--
-- Name: subscriptions; Type: TABLE; Schema: public; Owner: federation
--

CREATE TABLE public.subscriptions (
  sub_id integer NOT NULL,
  last_updated timestamp without time zone,
  path character varying(255),
  selector character varying(255),
  subscription_status character varying(255),
  spr_id_sub integer,
  ixn_id_sub_out integer,
  ixn_id_fed_in integer
);


ALTER TABLE public.subscriptions OWNER TO federation;

--
-- Name: dat_seq; Type: SEQUENCE SET; Schema: public; Owner: federation
--

SELECT pg_catalog.setval('public.dat_seq', 51, true);


--
-- Data for Name: data_types; Type: TABLE DATA; Schema: public; Owner: federation
--

COPY public.data_types (dat_id, how, last_updated, what, where1, spr_id_cap, ixn_id_cap) FROM stdin;
1	datex2;1.0	2019-03-29 13:48:38.007	Conditions	NO	1	\N
2	datex2;1.0	2019-03-29 13:48:38.01	Works	NO	1	\N
3	datex2;1.0	2019-03-29 13:49:10.011	Works	NO	2	\N
4	datex2;1.0	2019-03-29 13:49:10.012	Works	SE	2	\N
\.


--
-- Data for Name: interchanges; Type: TABLE DATA; Schema: public; Owner: federation
--

COPY public.interchanges (ixn_id, backoff_attempts, backoff_start, control_channel_port, domain_name, interchange_status, last_seen, last_updated, message_channel_port, name) FROM stdin;
\.


--
-- Name: ixn_seq; Type: SEQUENCE SET; Schema: public; Owner: federation
--

SELECT pg_catalog.setval('public.ixn_seq', 1, false);


--
-- Data for Name: service_providers; Type: TABLE DATA; Schema: public; Owner: federation
--

COPY public.service_providers (spr_id, name) FROM stdin;
1	Volvo Cloud
2	Scania Cloud
\.


--
-- Name: sp_generator; Type: SEQUENCE SET; Schema: public; Owner: federation
--

SELECT pg_catalog.setval('public.sp_generator', 51, true);


--
-- Name: sub_seq; Type: SEQUENCE SET; Schema: public; Owner: federation
--

SELECT pg_catalog.setval('public.sub_seq', 51, true);


--
-- Data for Name: subscriptions; Type: TABLE DATA; Schema: public; Owner: federation
--

COPY public.subscriptions (sub_id, last_updated, path, selector, subscription_status, spr_id_sub, ixn_id_sub_out, ixn_id_fed_in) FROM stdin;
1	2019-03-29 13:48:38.012	\N	where1 LIKE 'FI'	REQUESTED	1	\N	\N
2	2019-03-29 13:49:10.014	\N	where1 LIKE 'DK'	REQUESTED	2	\N	\N
\.


--
-- Name: data_types data_types_pkey; Type: CONSTRAINT; Schema: public; Owner: federation
--

ALTER TABLE ONLY public.data_types
  ADD CONSTRAINT data_types_pkey PRIMARY KEY (dat_id);


--
-- Name: interchanges interchanges_pkey; Type: CONSTRAINT; Schema: public; Owner: federation
--

ALTER TABLE ONLY public.interchanges
  ADD CONSTRAINT interchanges_pkey PRIMARY KEY (ixn_id);


--
-- Name: service_providers service_providers_pkey; Type: CONSTRAINT; Schema: public; Owner: federation
--

ALTER TABLE ONLY public.service_providers
  ADD CONSTRAINT service_providers_pkey PRIMARY KEY (spr_id);


--
-- Name: subscriptions subscriptions_pkey; Type: CONSTRAINT; Schema: public; Owner: federation
--

ALTER TABLE ONLY public.subscriptions
  ADD CONSTRAINT subscriptions_pkey PRIMARY KEY (sub_id);


--
-- Name: interchanges uk_ixn_name; Type: CONSTRAINT; Schema: public; Owner: federation
--

ALTER TABLE ONLY public.interchanges
  ADD CONSTRAINT uk_ixn_name UNIQUE (name);


--
-- Name: service_providers uk_spr_name; Type: CONSTRAINT; Schema: public; Owner: federation
--

ALTER TABLE ONLY public.service_providers
  ADD CONSTRAINT uk_spr_name UNIQUE (name);


--
-- Name: data_types fk_dat_ixn; Type: FK CONSTRAINT; Schema: public; Owner: federation
--

ALTER TABLE ONLY public.data_types
  ADD CONSTRAINT fk_dat_ixn FOREIGN KEY (ixn_id_cap) REFERENCES public.interchanges(ixn_id);


--
-- Name: data_types fk_dat_spr; Type: FK CONSTRAINT; Schema: public; Owner: federation
--

ALTER TABLE ONLY public.data_types
  ADD CONSTRAINT fk_dat_spr FOREIGN KEY (spr_id_cap) REFERENCES public.service_providers(spr_id);


--
-- Name: subscriptions fk_sub_ixn_fed_in; Type: FK CONSTRAINT; Schema: public; Owner: federation
--

ALTER TABLE ONLY public.subscriptions
  ADD CONSTRAINT fk_sub_ixn_fed_in FOREIGN KEY (ixn_id_fed_in) REFERENCES public.interchanges(ixn_id);


--
-- Name: subscriptions fk_sub_ixn_sub_out; Type: FK CONSTRAINT; Schema: public; Owner: federation
--

ALTER TABLE ONLY public.subscriptions
  ADD CONSTRAINT fk_sub_ixn_sub_out FOREIGN KEY (ixn_id_sub_out) REFERENCES public.interchanges(ixn_id);


--
-- Name: subscriptions fk_sub_spr; Type: FK CONSTRAINT; Schema: public; Owner: federation
--

ALTER TABLE ONLY public.subscriptions
  ADD CONSTRAINT fk_sub_spr FOREIGN KEY (spr_id_sub) REFERENCES public.service_providers(spr_id);


--
-- PostgreSQL database dump complete
--