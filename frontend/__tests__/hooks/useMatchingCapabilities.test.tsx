import React from "react";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { describe, it, expect, jest, afterEach } from "@jest/globals";
import { useMatchingCapabilities } from "@/hooks/useMatchingCapabilities";

const createWrapper = () => {
  const queryClient = new QueryClient();
  const Wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
  return Wrapper;
};

describe("useMatchingCapabilities", () => {
  const originalFetch = global.fetch;

  afterEach(() => {
    global.fetch = originalFetch;
    jest.restoreAllMocks();
  });

  it("omits blank selector parameter", async () => {
    const mockFetch = jest.fn<typeof fetch>().mockResolvedValue({
      ok: true,
      json: async () => [],
    } as Response);
    global.fetch = mockFetch;

    const { result } = renderHook(
      () => useMatchingCapabilities("actor.example.eu", "   "),
      { wrapper: createWrapper() }
    );

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(mockFetch).toHaveBeenCalledTimes(1);
    expect(mockFetch).toHaveBeenCalledWith(
      "/api/actor.example.eu/network/capabilities"
    );
  });

  it("surfaces 400 invalid selector error without retrying", async () => {
    const mockFetch = jest.fn<typeof fetch>().mockResolvedValue({
      ok: false,
      status: 400,
      json: async () => ({
        errorCode: "400 BAD_REQUEST",
        message:
          'Invalid selector "LIKE": Parse error at line 1, column 1.  Encountered: LIKE',
      }),
    } as Response);
    global.fetch = mockFetch;

    const { result } = renderHook(
      () => useMatchingCapabilities("actor.example.eu", "LIKE"),
      { wrapper: createWrapper() }
    );

    await waitFor(() => expect(result.current.isError).toBe(true));
    expect(mockFetch).toHaveBeenCalledTimes(1);
    expect((result.current.error as Error).message).toContain(
      'Invalid selector "LIKE"'
    );
  });
});
