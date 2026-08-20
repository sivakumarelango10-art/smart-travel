import { apiClient } from './api';
import { ApiResponse, CabinClass, DynamicPriceBreakdown, FlightPriceHistory, PriceFreeze } from '../types/api';

export const pricingService = {
  /**
   * Get dynamic price breakdown with itemized demand/seasonal/holiday adjustments.
   */
  async getPriceBreakdown(
    flightId: string,
    cabinClass: CabinClass = 'ECONOMY',
    passengers: number = 1
  ): Promise<DynamicPriceBreakdown> {
    const response = await apiClient.get<ApiResponse<DynamicPriceBreakdown>>(
      `/v1/pricing/flights/${flightId}/breakdown`,
      { params: { cabinClass, passengers } }
    );
    return response.data.data;
  },

  /**
   * Get price history trend points for charting.
   */
  async getPriceHistory(
    flightId: string,
    cabinClass?: CabinClass
  ): Promise<FlightPriceHistory[]> {
    const response = await apiClient.get<ApiResponse<{ content: FlightPriceHistory[] }>>(
      `/v1/pricing/flights/${flightId}/history`,
      { params: { cabinClass, size: 50 } }
    );
    return response.data.data?.content || [];
  },

  /**
   * Create a price freeze locking current fare for 30 minutes.
   */
  async createPriceFreeze(
    flightId: string,
    cabinClass: CabinClass,
    passengerCount: number = 1
  ): Promise<PriceFreeze> {
    const response = await apiClient.post<ApiResponse<PriceFreeze>>('/v1/price-freezes', {
      flightId,
      cabinClass,
      passengerCount,
    });
    return response.data.data;
  },

  /**
   * Get all active and past price freezes for user.
   */
  async getUserPriceFreezes(): Promise<PriceFreeze[]> {
    const response = await apiClient.get<ApiResponse<PriceFreeze[]>>('/v1/price-freezes');
    return response.data.data || [];
  },

  /**
   * Cancel an active price freeze.
   */
  async cancelPriceFreeze(freezeId: string): Promise<PriceFreeze> {
    const response = await apiClient.post<ApiResponse<PriceFreeze>>(`/v1/price-freezes/${freezeId}/cancel`);
    return response.data.data;
  },
};
