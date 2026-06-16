import React, { useState } from 'react';

// Estructura de datos que coincide exactamente con tu entidad JPA WarehouseReceipt
interface WarehouseReceiptForm {
  mawbNumber: string;
  shipperName: string;
  consigneeName: string;
  carrierName: string;
  observations: string;
}

export const WarehouseReceiptView: React.FC = () => {
  const [formData, setFormData] = useState<WarehouseReceiptForm>({
    mawbNumber: '',
    shipperName: '',
    consigneeName: '',
    carrierName: '',
    observations: ''
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    console.log("Datos del formulario capturados:", formData);
    // Aquí irá tu llamada fetch/axios al endpoint POST /api/warehouse/receipts/emit
    alert("Formulario capturado. Enviando a la API de bodega...");
  };

  return (
    <div className="max-w-4xl mx-auto p-8 bg-zinc-900 border border-zinc-800 rounded-xl shadow-2xl">
      <h2 className="text-2xl font-black text-amber-500 mb-6 uppercase tracking-widest border-b border-zinc-800 pb-4">
        Registro de Recibo de Almacén (CFS)
      </h2>
      
      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="space-y-2">
            <label className="text-xs font-mono text-zinc-400">MAWB NUMBER</label>
            <input 
              required
              className="w-full bg-zinc-950 border border-zinc-700 p-3 rounded text-white font-mono"
              value={formData.mawbNumber}
              onChange={(e) => setFormData({...formData, mawbNumber: e.target.value})}
            />
          </div>
          <div className="space-y-2">
            <label className="text-xs font-mono text-zinc-400">SHIPPER NAME</label>
            <input 
              required
              className="w-full bg-zinc-950 border border-zinc-700 p-3 rounded text-white font-mono"
              value={formData.shipperName}
              onChange={(e) => setFormData({...formData, shipperName: e.target.value})}
            />
          </div>
          <div className="space-y-2">
            <label className="text-xs font-mono text-zinc-400">CONSIGNEE NAME</label>
            <input 
              required
              className="w-full bg-zinc-950 border border-zinc-700 p-3 rounded text-white font-mono"
              value={formData.consigneeName}
              onChange={(e) => setFormData({...formData, consigneeName: e.target.value})}
            />
          </div>
          <div className="space-y-2">
            <label className="text-xs font-mono text-zinc-400">CARRIER</label>
            <input 
              required
              className="w-full bg-zinc-950 border border-zinc-700 p-3 rounded text-white font-mono"
              value={formData.carrierName}
              onChange={(e) => setFormData({...formData, carrierName: e.target.value})}
            />
          </div>
        </div>

        <div className="space-y-2">
          <label className="text-xs font-mono text-zinc-400">OBSERVACIONES DE RAMPA</label>
          <textarea 
            className="w-full bg-zinc-950 border border-zinc-700 p-3 rounded text-white font-mono h-24"
            value={formData.observations}
            onChange={(e) => setFormData({...formData, observations: e.target.value})}
          />
        </div>

        <button 
          type="submit"
          className="w-full bg-amber-600 hover:bg-amber-500 text-zinc-950 font-bold py-4 rounded uppercase tracking-widest transition"
        >
          Iniciar Proceso de Cubicaje
        </button>
      </form>
    </div>
  );
};
