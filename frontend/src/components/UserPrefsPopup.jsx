import { createPortal } from 'react-dom'
import { useEffect, useState } from 'react'
import '../styling/popup.css'

export default function UserPrefsPopup({isOpen, isLoading, errorMessage, title, onClose, onSubmit}) {
    const [value, setValue] = useState('')
    const [selectedTrait, setSelectedTrait] = useState('BREED')

    // Reset form when opened
    useEffect(() => {
        if (isOpen) {
            setValue('')
            setSelectedTrait('BREED')
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = 'unset';
        }
        return () => { document.body.style.overflow = 'unset'; };
    }, [isOpen])

    const handlePost = (e) => {
        e.preventDefault();
        if (value.toString().trim()) {
            // Returns both the type (e.g., 'size') and the choice (e.g., 'Large')
            onSubmit(selectedTrait, value)
            onClose()
        }
    }

    if (!isOpen) return null
    
    return createPortal(
        <div className="popup-overlay" onClick={onClose}>
            <div className="popup-content" onClick={(e) => e.stopPropagation()}>
                <form onSubmit={handlePost}>
                    <h3>{title}</h3>
                    
                    <div className="input-container">
                        <label className="popup-label">Select Trait</label>
                        <select 
                            className="trait-dropdown"
                            value={selectedTrait} 
                            onChange={(e) => {
                                setSelectedTrait(e.target.value);
                                setValue(''); // Reset value when switching traits
                            }}
                        >
                            <option value="BREED">Breed</option>
                            <option value="SIZE">Size</option>
                            <option value="GENDER">Gender</option>
                            <option value="AGE_MIN">Minimum Age</option>
                            <option value="AGE_MAX">Maximum Age</option>
                        </select>
                    </div>

                    <div className="input-container">
                        <label className="popup-label">Value</label>
                        {/* DYNAMIC INPUT FIELD */}
                        {selectedTrait === 'BREED' && (
                            <input 
                                type="text" 
                                placeholder="e.g. Golden Retriever"
                                value={value}
                                onChange={(e) => setValue(e.target.value)}
                                required
                            />
                        )}

                        {selectedTrait === 'SIZE' && (
                            <select value={value} onChange={(e) => setValue(e.target.value)} required>
                                <option value="">Select Size...</option>
                                <option value="Small">Small</option>
                                <option value="Medium">Medium</option>
                                <option value="Large">Large</option>
                                <option value="Extra Large">Extra Large</option>
                            </select>
                        )}

                        {selectedTrait === 'GENDER' && (
                            <select value={value} onChange={(e) => setValue(e.target.value)} required>
                                <option value="">Select Gender...</option>
                                <option value="Male">Male</option>
                                <option value="Female">Female</option>
                                <option value="Any">Any</option>
                            </select>
                        )}

                        {(selectedTrait === 'AGE_MIN' || selectedTrait === 'AGE_MAX') && (
                            <input 
                                type="number" 
                                min="0" 
                                max="25"
                                placeholder="Enter age in years"
                                value={value}
                                onChange={(e) => setValue(e.target.value)}
                                required
                            />
                        )}
                    </div>

                    <div className="popup-actions">
                        <button className="cancel-btn" type="button" onClick={onClose}>
                            Cancel
                        </button>
                        <button 
                            type="submit" 
                            className="submit-btn"
                            disabled={isLoading || !value.toString().trim()}
                        >
                            {isLoading ? 'Adding...' : 'Add Preference'}
                        </button>
                    </div>

                    {errorMessage && <p className="popup-error">{errorMessage}</p>}
                </form>
            </div>
        </div>,
        document.body
    );
}