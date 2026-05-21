export interface BooBridge {
  call<TReq, TRes>(namespace: string, method: string, payload: TReq): Promise<TRes>;
}
