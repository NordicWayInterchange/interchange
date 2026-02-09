export type CertificateSignRequest = {
  csr: string;
};

export type CertificateSignResponse = {
  chain: Array<string>;
};
