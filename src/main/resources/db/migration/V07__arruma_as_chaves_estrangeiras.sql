ALTER TABLE public.lote
    ADD FOREIGN KEY (codigo_vacina)
    REFERENCES public.vacina (codigo)
    NOT VALID;


ALTER TABLE public.aplicacao
    ADD FOREIGN KEY (codigo_pessoa)
    REFERENCES public.pessoa (codigo)
    NOT VALID;


ALTER TABLE public.aplicacao
    ADD FOREIGN KEY (codigo_lote)
    REFERENCES public.lote (codigo)
    NOT VALID;